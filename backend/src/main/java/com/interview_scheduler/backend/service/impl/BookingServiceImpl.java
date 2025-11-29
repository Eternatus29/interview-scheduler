package com.interview_scheduler.backend.service.impl;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import com.interview_scheduler.backend.dto.request.BookSlotRequest;
import com.interview_scheduler.backend.dto.request.UpdateBookingRequest;
import com.interview_scheduler.backend.dto.response.BookingResponse;
import com.interview_scheduler.backend.entity.Booking;
import com.interview_scheduler.backend.entity.BookingStatus;
import com.interview_scheduler.backend.entity.Candidate;
import com.interview_scheduler.backend.entity.InterviewSlot;
import com.interview_scheduler.backend.entity.SlotStatus;
import com.interview_scheduler.backend.exception.DuplicateBookingException;
import com.interview_scheduler.backend.exception.MaxInterviewsExceededException;
import com.interview_scheduler.backend.exception.ResourceNotFoundException;
import com.interview_scheduler.backend.exception.SlotAlreadyBookedException;
import com.interview_scheduler.backend.exception.SlotNotAvailableException;
import com.interview_scheduler.backend.repository.BookingRepository;
import com.interview_scheduler.backend.repository.CandidateRepository;
import com.interview_scheduler.backend.repository.InterviewSlotRepository;
import com.interview_scheduler.backend.service.BookingService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final InterviewSlotRepository slotRepository;
    private final CandidateRepository candidateRepository;

    private static final int BOOKING_WINDOW_WEEKS = 2;

    @Override
    @Transactional(isolation = Isolation.SERIALIZABLE)
    @Retryable(retryFor = {
            org.springframework.orm.ObjectOptimisticLockingFailureException.class }, maxAttempts = 3, backoff = @Backoff(delay = 100, multiplier = 2))
    public BookingResponse bookSlot(BookSlotRequest request) {
        log.info("Attempting to book slot {} for candidate {}", request.getSlotId(), request.getCandidateId());

        Candidate candidate = candidateRepository.findById(request.getCandidateId())
                .orElseThrow(() -> new ResourceNotFoundException("Candidate", "id", request.getCandidateId()));

        InterviewSlot slot = slotRepository.findByIdWithLock(request.getSlotId())
                .orElseThrow(() -> new ResourceNotFoundException("Interview Slot", "id", request.getSlotId()));

        if (slot.getStatus() != SlotStatus.AVAILABLE) {
            throw new SlotAlreadyBookedException(request.getSlotId());
        }

        if (slot.getStartTime().isBefore(LocalDateTime.now())) {
            throw new SlotNotAvailableException(request.getSlotId(), "Slot is in the past");
        }

        LocalDateTime windowStart = LocalDateTime.now();
        LocalDateTime windowEnd = windowStart.plusWeeks(BOOKING_WINDOW_WEEKS);

        if (bookingRepository.hasActiveBookingInDateRange(candidate.getId(), windowStart, windowEnd)) {
            throw new DuplicateBookingException(candidate.getId());
        }

        long bookedCount = slotRepository.countBookedSlotsForWeek(
                slot.getInterviewer().getId(),
                slot.getWeekNumber(),
                slot.getYear(),
                Arrays.asList(SlotStatus.BOOKED, SlotStatus.CONFIRMED));

        if (bookedCount >= slot.getInterviewer().getMaxInterviewsPerWeek()) {
            throw new MaxInterviewsExceededException(
                    slot.getInterviewer().getId(),
                    slot.getInterviewer().getMaxInterviewsPerWeek(),
                    slot.getWeekNumber());
        }

        slot.markAsBooked();
        slotRepository.save(slot);

        Booking booking = Booking.builder()
                .candidate(candidate)
                .slot(slot)
                .status(BookingStatus.PENDING)
                .weekNumber(slot.getWeekNumber())
                .year(slot.getYear())
                .bookingNotes(request.getBookingNotes())
                .createdAt(LocalDateTime.now())
                .build();

        booking = bookingRepository.save(booking);

        log.info("Successfully booked slot {} for candidate {}. Booking ID: {}",
                request.getSlotId(), request.getCandidateId(), booking.getId());

        return mapToResponse(booking);
    }

    @Override
    @Transactional(isolation = Isolation.SERIALIZABLE)
    @Retryable(retryFor = {
            org.springframework.orm.ObjectOptimisticLockingFailureException.class }, maxAttempts = 3, backoff = @Backoff(delay = 100, multiplier = 2))
    public BookingResponse updateBooking(Long bookingId, UpdateBookingRequest request) {
        log.info("Updating booking {} to new slot {}", bookingId, request.getNewSlotId());

        Booking existingBooking = bookingRepository.findByIdWithLock(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking", "id", bookingId));

        if (existingBooking.getStatus() == BookingStatus.CANCELLED) {
            throw new SlotNotAvailableException(existingBooking.getSlot().getId(), "Booking is already cancelled");
        }

        InterviewSlot newSlot = slotRepository.findByIdWithLock(request.getNewSlotId())
                .orElseThrow(() -> new ResourceNotFoundException("Interview Slot", "id", request.getNewSlotId()));

        if (newSlot.getStatus() != SlotStatus.AVAILABLE) {
            throw new SlotAlreadyBookedException(request.getNewSlotId());
        }

        if (newSlot.getStartTime().isBefore(LocalDateTime.now())) {
            throw new SlotNotAvailableException(request.getNewSlotId(), "New slot is in the past");
        }

        long bookedCount = slotRepository.countBookedSlotsForWeek(
                newSlot.getInterviewer().getId(),
                newSlot.getWeekNumber(),
                newSlot.getYear(),
                Arrays.asList(SlotStatus.BOOKED, SlotStatus.CONFIRMED));

        if (bookedCount >= newSlot.getInterviewer().getMaxInterviewsPerWeek()) {
            throw new MaxInterviewsExceededException(
                    newSlot.getInterviewer().getId(),
                    newSlot.getInterviewer().getMaxInterviewsPerWeek(),
                    newSlot.getWeekNumber());
        }

        InterviewSlot oldSlot = existingBooking.getSlot();
        oldSlot.markAsAvailable();
        slotRepository.save(oldSlot);

        newSlot.markAsBooked();
        slotRepository.save(newSlot);

        existingBooking.setSlot(newSlot);
        existingBooking.setWeekNumber(newSlot.getWeekNumber());
        existingBooking.setYear(newSlot.getYear());
        existingBooking.setUpdatedAt(LocalDateTime.now());
        if (request.getBookingNotes() != null) {
            existingBooking.setBookingNotes(request.getBookingNotes());
        }

        existingBooking = bookingRepository.save(existingBooking);

        log.info("Successfully updated booking {} to new slot {}", bookingId, request.getNewSlotId());

        return mapToResponse(existingBooking);
    }

    @Override
    @Transactional
    public BookingResponse cancelBooking(Long bookingId) {
        log.info("Cancelling booking {}", bookingId);

        Booking booking = bookingRepository.findByIdWithLock(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking", "id", bookingId));

        if (booking.getStatus() == BookingStatus.CANCELLED) {
            throw new SlotNotAvailableException(booking.getSlot().getId(), "Booking is already cancelled");
        }

        InterviewSlot slot = booking.getSlot();
        slot.markAsAvailable();
        slotRepository.save(slot);

        booking.cancel();
        booking = bookingRepository.save(booking);

        log.info("Successfully cancelled booking {}", bookingId);

        return mapToResponse(booking);
    }

    @Override
    @Transactional
    public BookingResponse confirmBooking(Long bookingId) {
        log.info("Confirming booking {}", bookingId);

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking", "id", bookingId));

        if (booking.getStatus() != BookingStatus.PENDING) {
            throw new SlotNotAvailableException(booking.getSlot().getId(),
                    "Booking cannot be confirmed. Current status: " + booking.getStatus());
        }

        booking.confirm();
        booking = bookingRepository.save(booking);

        InterviewSlot slot = booking.getSlot();
        slot.markAsConfirmed();
        slotRepository.save(slot);

        log.info("Successfully confirmed booking {}", bookingId);

        return mapToResponse(booking);
    }

    @Override
    @Transactional(readOnly = true)
    public BookingResponse getBookingById(Long id) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Booking", "id", id));
        return mapToResponse(booking);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookingResponse> getBookingsByCandidateId(Long candidateId) {
        if (!candidateRepository.existsById(candidateId)) {
            throw new ResourceNotFoundException("Candidate", "id", candidateId);
        }

        return bookingRepository.findByCandidateId(candidateId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public BookingResponse getBookingBySlotId(Long slotId) {
        Booking booking = bookingRepository.findBySlotId(slotId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking", "slotId", slotId));
        return mapToResponse(booking);
    }

    private BookingResponse mapToResponse(Booking booking) {
        InterviewSlot slot = booking.getSlot();
        Candidate candidate = booking.getCandidate();

        return BookingResponse.builder()
                .id(booking.getId())
                .candidateId(candidate.getId())
                .candidateName(candidate.getName())
                .candidateEmail(candidate.getEmail())
                .slotId(slot.getId())
                .slotStartTime(slot.getStartTime())
                .slotEndTime(slot.getEndTime())
                .interviewerId(slot.getInterviewer().getId())
                .interviewerName(slot.getInterviewer().getName())
                .status(booking.getStatus())
                .bookingNotes(booking.getBookingNotes())
                .weekNumber(booking.getWeekNumber())
                .year(booking.getYear())
                .createdAt(booking.getCreatedAt())
                .confirmedAt(booking.getConfirmedAt())
                .build();
    }
}
