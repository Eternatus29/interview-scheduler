package com.interview_scheduler.backend.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.interview_scheduler.backend.dto.request.BookSlotRequest;
import com.interview_scheduler.backend.dto.request.UpdateBookingRequest;
import com.interview_scheduler.backend.dto.response.BookingResponse;
import com.interview_scheduler.backend.entity.Booking;
import com.interview_scheduler.backend.entity.BookingStatus;
import com.interview_scheduler.backend.entity.Candidate;
import com.interview_scheduler.backend.entity.Interviewer;
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
import com.interview_scheduler.backend.service.impl.BookingServiceImpl;

@ExtendWith(MockitoExtension.class)
@DisplayName("Booking Service Tests")
class BookingServiceTest {

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private InterviewSlotRepository slotRepository;

    @Mock
    private CandidateRepository candidateRepository;

    @InjectMocks
    private BookingServiceImpl bookingService;

    private Candidate testCandidate;
    private Interviewer testInterviewer;
    private InterviewSlot testSlot;
    private Booking testBooking;

    @BeforeEach
    void setUp() {
        testInterviewer = Interviewer.builder()
                .id(1L)
                .name("Test Interviewer")
                .email("interviewer@test.com")
                .maxInterviewsPerWeek(10)
                .slotDurationMinutes(60)
                .build();

        testCandidate = Candidate.builder()
                .id(1L)
                .name("Test Candidate")
                .email("candidate@test.com")
                .phoneNumber("+1234567890")
                .build();

        testSlot = InterviewSlot.builder()
                .id(1L)
                .interviewer(testInterviewer)
                .startTime(LocalDateTime.now().plusDays(1).withHour(10).withMinute(0))
                .endTime(LocalDateTime.now().plusDays(1).withHour(11).withMinute(0))
                .status(SlotStatus.AVAILABLE)
                .weekNumber(1)
                .year(2025)
                .build();

        testBooking = Booking.builder()
                .id(1L)
                .candidate(testCandidate)
                .slot(testSlot)
                .status(BookingStatus.PENDING)
                .weekNumber(1)
                .year(2025)
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Nested
    @DisplayName("Book Slot Tests")
    class BookSlotTests {

        @Test
        @DisplayName("Should successfully book an available slot")
        void bookSlot_Success() {
            BookSlotRequest request = BookSlotRequest.builder()
                    .slotId(1L)
                    .candidateId(1L)
                    .bookingNotes("Test booking")
                    .build();

            when(candidateRepository.findById(1L)).thenReturn(Optional.of(testCandidate));
            when(slotRepository.findByIdWithLock(1L)).thenReturn(Optional.of(testSlot));
            when(bookingRepository.hasActiveBookingInDateRange(anyLong(), any(), any())).thenReturn(false);
            when(slotRepository.countBookedSlotsForWeek(anyLong(), anyInt(), anyInt(), anyList())).thenReturn(0L);
            when(slotRepository.save(any(InterviewSlot.class))).thenReturn(testSlot);
            when(bookingRepository.save(any(Booking.class))).thenReturn(testBooking);

            BookingResponse response = bookingService.bookSlot(request);

            assertNotNull(response);
            assertEquals(testCandidate.getId(), response.getCandidateId());
            assertEquals(testSlot.getId(), response.getSlotId());
            verify(slotRepository).save(any(InterviewSlot.class));
            verify(bookingRepository).save(any(Booking.class));
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException for non-existent candidate")
        void bookSlot_CandidateNotFound() {
            BookSlotRequest request = BookSlotRequest.builder()
                    .slotId(1L)
                    .candidateId(999L)
                    .build();

            when(candidateRepository.findById(999L)).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class, () -> bookingService.bookSlot(request));
            verify(bookingRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException for non-existent slot")
        void bookSlot_SlotNotFound() {
            BookSlotRequest request = BookSlotRequest.builder()
                    .slotId(999L)
                    .candidateId(1L)
                    .build();

            when(candidateRepository.findById(1L)).thenReturn(Optional.of(testCandidate));
            when(slotRepository.findByIdWithLock(999L)).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class, () -> bookingService.bookSlot(request));
            verify(bookingRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw SlotAlreadyBookedException when slot is not available")
        void bookSlot_SlotAlreadyBooked() {
            testSlot.setStatus(SlotStatus.BOOKED);
            BookSlotRequest request = BookSlotRequest.builder()
                    .slotId(1L)
                    .candidateId(1L)
                    .build();

            when(candidateRepository.findById(1L)).thenReturn(Optional.of(testCandidate));
            when(slotRepository.findByIdWithLock(1L)).thenReturn(Optional.of(testSlot));

            assertThrows(SlotAlreadyBookedException.class, () -> bookingService.bookSlot(request));
            verify(bookingRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw SlotNotAvailableException when slot is in the past")
        void bookSlot_SlotInPast() {
            testSlot.setStartTime(LocalDateTime.now().minusHours(1));
            BookSlotRequest request = BookSlotRequest.builder()
                    .slotId(1L)
                    .candidateId(1L)
                    .build();

            when(candidateRepository.findById(1L)).thenReturn(Optional.of(testCandidate));
            when(slotRepository.findByIdWithLock(1L)).thenReturn(Optional.of(testSlot));

            assertThrows(SlotNotAvailableException.class, () -> bookingService.bookSlot(request));
            verify(bookingRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw DuplicateBookingException when candidate has existing booking")
        void bookSlot_DuplicateBooking() {
            BookSlotRequest request = BookSlotRequest.builder()
                    .slotId(1L)
                    .candidateId(1L)
                    .build();

            when(candidateRepository.findById(1L)).thenReturn(Optional.of(testCandidate));
            when(slotRepository.findByIdWithLock(1L)).thenReturn(Optional.of(testSlot));
            when(bookingRepository.hasActiveBookingInDateRange(anyLong(), any(), any())).thenReturn(true);

            assertThrows(DuplicateBookingException.class, () -> bookingService.bookSlot(request));
            verify(bookingRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw MaxInterviewsExceededException when interviewer is at capacity")
        void bookSlot_MaxInterviewsExceeded() {
            testInterviewer.setMaxInterviewsPerWeek(5);
            BookSlotRequest request = BookSlotRequest.builder()
                    .slotId(1L)
                    .candidateId(1L)
                    .build();

            when(candidateRepository.findById(1L)).thenReturn(Optional.of(testCandidate));
            when(slotRepository.findByIdWithLock(1L)).thenReturn(Optional.of(testSlot));
            when(bookingRepository.hasActiveBookingInDateRange(anyLong(), any(), any())).thenReturn(false);
            when(slotRepository.countBookedSlotsForWeek(anyLong(), anyInt(), anyInt(), anyList())).thenReturn(5L);

            assertThrows(MaxInterviewsExceededException.class, () -> bookingService.bookSlot(request));
            verify(bookingRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("Update Booking Tests")
    class UpdateBookingTests {

        private InterviewSlot newSlot;

        @BeforeEach
        void setUpNewSlot() {
            newSlot = InterviewSlot.builder()
                    .id(2L)
                    .interviewer(testInterviewer)
                    .startTime(LocalDateTime.now().plusDays(2).withHour(14).withMinute(0))
                    .endTime(LocalDateTime.now().plusDays(2).withHour(15).withMinute(0))
                    .status(SlotStatus.AVAILABLE)
                    .weekNumber(1)
                    .year(2025)
                    .build();
        }

        @Test
        @DisplayName("Should successfully update booking to new slot")
        void updateBooking_Success() {
            testSlot.setStatus(SlotStatus.BOOKED);
            UpdateBookingRequest request = UpdateBookingRequest.builder()
                    .newSlotId(2L)
                    .bookingNotes("Rescheduled")
                    .build();

            when(bookingRepository.findByIdWithLock(1L)).thenReturn(Optional.of(testBooking));
            when(slotRepository.findByIdWithLock(2L)).thenReturn(Optional.of(newSlot));
            when(slotRepository.countBookedSlotsForWeek(anyLong(), anyInt(), anyInt(), anyList())).thenReturn(0L);
            when(slotRepository.save(any(InterviewSlot.class))).thenAnswer(i -> i.getArgument(0));
            when(bookingRepository.save(any(Booking.class))).thenReturn(testBooking);

            BookingResponse response = bookingService.updateBooking(1L, request);

            assertNotNull(response);
            verify(slotRepository, times(2)).save(any(InterviewSlot.class));
            verify(bookingRepository).save(any(Booking.class));
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException for non-existent booking")
        void updateBooking_BookingNotFound() {
            UpdateBookingRequest request = UpdateBookingRequest.builder()
                    .newSlotId(2L)
                    .build();

            when(bookingRepository.findByIdWithLock(999L)).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class,
                    () -> bookingService.updateBooking(999L, request));
        }

        @Test
        @DisplayName("Should throw exception for already cancelled booking")
        void updateBooking_AlreadyCancelled() {
            testBooking.setStatus(BookingStatus.CANCELLED);
            UpdateBookingRequest request = UpdateBookingRequest.builder()
                    .newSlotId(2L)
                    .build();

            when(bookingRepository.findByIdWithLock(1L)).thenReturn(Optional.of(testBooking));

            assertThrows(SlotNotAvailableException.class,
                    () -> bookingService.updateBooking(1L, request));
        }
    }

    @Nested
    @DisplayName("Cancel Booking Tests")
    class CancelBookingTests {

        @Test
        @DisplayName("Should successfully cancel a booking")
        void cancelBooking_Success() {
            testSlot.setStatus(SlotStatus.BOOKED);
            testBooking.setSlot(testSlot);

            when(bookingRepository.findByIdWithLock(1L)).thenReturn(Optional.of(testBooking));
            when(slotRepository.save(any(InterviewSlot.class))).thenReturn(testSlot);
            when(bookingRepository.save(any(Booking.class))).thenReturn(testBooking);

            BookingResponse response = bookingService.cancelBooking(1L);

            assertNotNull(response);
            verify(slotRepository).save(any(InterviewSlot.class));
            verify(bookingRepository).save(any(Booking.class));
        }

        @Test
        @DisplayName("Should throw exception when cancelling already cancelled booking")
        void cancelBooking_AlreadyCancelled() {
            testBooking.setStatus(BookingStatus.CANCELLED);

            when(bookingRepository.findByIdWithLock(1L)).thenReturn(Optional.of(testBooking));

            assertThrows(SlotNotAvailableException.class,
                    () -> bookingService.cancelBooking(1L));
        }
    }

    @Nested
    @DisplayName("Confirm Booking Tests")
    class ConfirmBookingTests {

        @Test
        @DisplayName("Should successfully confirm a pending booking")
        void confirmBooking_Success() {
            when(bookingRepository.findById(1L)).thenReturn(Optional.of(testBooking));
            when(bookingRepository.save(any(Booking.class))).thenReturn(testBooking);
            when(slotRepository.save(any(InterviewSlot.class))).thenReturn(testSlot);

            BookingResponse response = bookingService.confirmBooking(1L);

            assertNotNull(response);
            verify(bookingRepository).save(any(Booking.class));
            verify(slotRepository).save(any(InterviewSlot.class));
        }

        @Test
        @DisplayName("Should throw exception when confirming non-pending booking")
        void confirmBooking_NotPending() {
            testBooking.setStatus(BookingStatus.CANCELLED);

            when(bookingRepository.findById(1L)).thenReturn(Optional.of(testBooking));

            assertThrows(SlotNotAvailableException.class,
                    () -> bookingService.confirmBooking(1L));
        }
    }
}
