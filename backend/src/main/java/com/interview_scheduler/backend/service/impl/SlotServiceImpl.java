package com.interview_scheduler.backend.service.impl;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.IsoFields;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.interview_scheduler.backend.dto.request.GenerateSlotsRequest;
import com.interview_scheduler.backend.dto.request.WeeklyAvailabilityRequest;
import com.interview_scheduler.backend.dto.response.InterviewSlotResponse;
import com.interview_scheduler.backend.dto.response.PaginatedResponse;
import com.interview_scheduler.backend.entity.Interviewer;
import com.interview_scheduler.backend.entity.InterviewSlot;
import com.interview_scheduler.backend.entity.SlotStatus;
import com.interview_scheduler.backend.entity.WeeklyAvailability;
import com.interview_scheduler.backend.exception.ResourceNotFoundException;
import com.interview_scheduler.backend.exception.ValidationException;
import com.interview_scheduler.backend.repository.InterviewerRepository;
import com.interview_scheduler.backend.repository.InterviewSlotRepository;
import com.interview_scheduler.backend.repository.WeeklyAvailabilityRepository;
import com.interview_scheduler.backend.service.SlotService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class SlotServiceImpl implements SlotService {

    private final InterviewSlotRepository slotRepository;
    private final InterviewerRepository interviewerRepository;
    private final WeeklyAvailabilityRepository weeklyAvailabilityRepository;

    @Override
    @Transactional
    public List<InterviewSlotResponse> generateSlots(GenerateSlotsRequest request) {
        log.info("Generating slots for interviewer: {}", request.getInterviewerId());

        Interviewer interviewer = interviewerRepository.findById(request.getInterviewerId())
                .orElseThrow(() -> new ResourceNotFoundException("Interviewer", "id", request.getInterviewerId()));

        if (request.getWeeklyAvailabilities() != null && !request.getWeeklyAvailabilities().isEmpty()) {
            updateWeeklyAvailabilities(interviewer, request.getWeeklyAvailabilities());
        }

        List<WeeklyAvailability> availabilities = weeklyAvailabilityRepository
                .findByInterviewerIdAndIsActiveTrue(interviewer.getId());

        if (availabilities.isEmpty()) {
            throw new ValidationException("No active weekly availability defined for this interviewer");
        }

        int weeksToGenerate = request.getWeeksToGenerate() != null ? request.getWeeksToGenerate() : 2;
        int slotDuration = interviewer.getSlotDurationMinutes();

        List<InterviewSlot> generatedSlots = new ArrayList<>();
        LocalDate today = LocalDate.now();

        for (int week = 0; week < weeksToGenerate; week++) {
            LocalDate weekStart = today.plusWeeks(week);

            for (WeeklyAvailability availability : availabilities) {
                LocalDate slotDate = weekStart.with(TemporalAdjusters.nextOrSame(availability.getDayOfWeek()));

                if (slotDate.isBefore(today)) {
                    slotDate = slotDate.plusWeeks(1);
                }

                if (slotDate.isAfter(today.plusWeeks(weeksToGenerate))) {
                    continue;
                }

                LocalTime currentTime = availability.getStartTime();
                while (currentTime.plusMinutes(slotDuration).isBefore(availability.getEndTime()) ||
                        currentTime.plusMinutes(slotDuration).equals(availability.getEndTime())) {

                    LocalDateTime slotStart = LocalDateTime.of(slotDate, currentTime);
                    LocalDateTime slotEnd = slotStart.plusMinutes(slotDuration);

                    if (slotStart.isBefore(LocalDateTime.now())) {
                        currentTime = currentTime.plusMinutes(slotDuration);
                        continue;
                    }

                    if (slotRepository.findByInterviewerIdAndStartTime(interviewer.getId(), slotStart).isEmpty()) {
                        InterviewSlot slot = InterviewSlot.builder()
                                .interviewer(interviewer)
                                .startTime(slotStart)
                                .endTime(slotEnd)
                                .status(SlotStatus.AVAILABLE)
                                .weekNumber(slotStart.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR))
                                .year(slotStart.getYear())
                                .createdAt(LocalDateTime.now())
                                .build();

                        generatedSlots.add(slotRepository.save(slot));
                    }

                    currentTime = currentTime.plusMinutes(slotDuration);
                }
            }
        }

        log.info("Generated {} slots for interviewer: {}", generatedSlots.size(), interviewer.getId());

        return generatedSlots.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public PaginatedResponse<InterviewSlotResponse> getAvailableSlots(int page, int size) {
        LocalDateTime now = LocalDateTime.now();
        Pageable pageable = PageRequest.of(page, size);

        Page<InterviewSlot> slotsPage = slotRepository.findAvailableSlotsPageable(
                SlotStatus.AVAILABLE, now, pageable);

        List<InterviewSlotResponse> slots = slotsPage.getContent().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        return PaginatedResponse.ofOffset(
                slots,
                page,
                size,
                slotsPage.getTotalElements(),
                slotsPage.getTotalPages());
    }

    @Override
    @Transactional(readOnly = true)
    public PaginatedResponse<InterviewSlotResponse> getAvailableSlots(int page, int size, Long interviewerId) {
        if (interviewerId == null) {
            return getAvailableSlots(page, size);
        }

        LocalDateTime now = LocalDateTime.now();
        Pageable pageable = PageRequest.of(page, size);

        Page<InterviewSlot> slotsPage = slotRepository.findAvailableSlotsPageableByInterviewer(
                SlotStatus.AVAILABLE, now, interviewerId, pageable);

        List<InterviewSlotResponse> slots = slotsPage.getContent().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        return PaginatedResponse.ofOffset(
                slots,
                page,
                size,
                slotsPage.getTotalElements(),
                slotsPage.getTotalPages());
    }

    @Override
    @Transactional(readOnly = true)
    public PaginatedResponse<InterviewSlotResponse> getAvailableSlotsByCursor(Long cursor, int limit) {
        LocalDateTime now = LocalDateTime.now();

        Long effectiveCursor = cursor != null ? cursor : 0L;

        Pageable pageable = PageRequest.of(0, limit + 1);
        List<InterviewSlot> slots = slotRepository.findAvailableSlotsByCursor(
                SlotStatus.AVAILABLE, now, effectiveCursor, pageable);

        boolean hasNext = slots.size() > limit;
        if (hasNext) {
            slots = slots.subList(0, limit);
        }

        List<InterviewSlotResponse> responses = slots.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        Long nextCursor = !slots.isEmpty() ? slots.get(slots.size() - 1).getId() : null;

        return PaginatedResponse.ofCursor(responses, nextCursor, effectiveCursor, hasNext);
    }

    @Override
    @Transactional(readOnly = true)
    public PaginatedResponse<InterviewSlotResponse> getAvailableSlotsByCursor(Long cursor, int limit,
            Long interviewerId) {
        if (interviewerId == null) {
            return getAvailableSlotsByCursor(cursor, limit);
        }

        LocalDateTime now = LocalDateTime.now();

        Long effectiveCursor = cursor != null ? cursor : 0L;

        Pageable pageable = PageRequest.of(0, limit + 1);
        List<InterviewSlot> slots = slotRepository.findAvailableSlotsByCursorByInterviewer(
                SlotStatus.AVAILABLE, now, interviewerId, effectiveCursor, pageable);

        boolean hasNext = slots.size() > limit;
        if (hasNext) {
            slots = slots.subList(0, limit);
        }

        List<InterviewSlotResponse> responses = slots.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        Long nextCursor = !slots.isEmpty() ? slots.get(slots.size() - 1).getId() : null;

        return PaginatedResponse.ofCursor(responses, nextCursor, effectiveCursor, hasNext);
    }

    @Override
    @Transactional(readOnly = true)
    public List<InterviewSlotResponse> getAvailableSlotsForInterviewer(Long interviewerId) {
        if (!interviewerRepository.existsById(interviewerId)) {
            throw new ResourceNotFoundException("Interviewer", "id", interviewerId);
        }

        return slotRepository.findByInterviewerIdAndStatus(interviewerId, SlotStatus.AVAILABLE)
                .stream()
                .filter(slot -> slot.getStartTime().isAfter(LocalDateTime.now()))
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public InterviewSlotResponse getSlotById(Long id) {
        InterviewSlot slot = slotRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Interview Slot", "id", id));
        return mapToResponse(slot);
    }

    @Override
    @Transactional
    public int markExpiredSlots() {
        LocalDateTime now = LocalDateTime.now();
        List<InterviewSlot> expiredSlots = slotRepository.findExpiredAvailableSlots(now);

        for (InterviewSlot slot : expiredSlots) {
            slot.setStatus(SlotStatus.EXPIRED);
            slot.setUpdatedAt(now);
            slotRepository.save(slot);
        }

        log.info("Marked {} slots as expired", expiredSlots.size());
        return expiredSlots.size();
    }

    private void updateWeeklyAvailabilities(Interviewer interviewer,
            List<WeeklyAvailabilityRequest> availabilities) {
        List<WeeklyAvailability> existing = weeklyAvailabilityRepository
                .findByInterviewerId(interviewer.getId());
        for (WeeklyAvailability wa : existing) {
            wa.setIsActive(false);
        }
        weeklyAvailabilityRepository.saveAll(existing);

        for (WeeklyAvailabilityRequest req : availabilities) {
            if (req.getEndTime() == null || req.getStartTime() == null ||
                    !req.getEndTime().isAfter(req.getStartTime())) {
                throw new ValidationException("End time must be after start time");
            }

            WeeklyAvailability availability = WeeklyAvailability.builder()
                    .interviewer(interviewer)
                    .dayOfWeek(req.getDayOfWeek())
                    .startTime(req.getStartTime())
                    .endTime(req.getEndTime())
                    .isActive(true)
                    .build();

            weeklyAvailabilityRepository.save(availability);
        }
    }

    private InterviewSlotResponse mapToResponse(InterviewSlot slot) {
        return InterviewSlotResponse.builder()
                .id(slot.getId())
                .interviewerId(slot.getInterviewer().getId())
                .interviewerName(slot.getInterviewer().getName())
                .startTime(slot.getStartTime())
                .endTime(slot.getEndTime())
                .status(slot.getStatus())
                .weekNumber(slot.getWeekNumber())
                .year(slot.getYear())
                .build();
    }
}
