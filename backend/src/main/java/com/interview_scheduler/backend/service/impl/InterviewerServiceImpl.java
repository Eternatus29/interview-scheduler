package com.interview_scheduler.backend.service.impl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.interview_scheduler.backend.dto.request.CreateInterviewerRequest;
import com.interview_scheduler.backend.dto.request.WeeklyAvailabilityRequest;
import com.interview_scheduler.backend.dto.response.InterviewerResponse;
import com.interview_scheduler.backend.dto.response.WeeklyAvailabilityResponse;
import com.interview_scheduler.backend.entity.Interviewer;
import com.interview_scheduler.backend.entity.WeeklyAvailability;
import com.interview_scheduler.backend.exception.ResourceNotFoundException;
import com.interview_scheduler.backend.exception.ValidationException;
import com.interview_scheduler.backend.repository.InterviewerRepository;
import com.interview_scheduler.backend.service.InterviewerService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class InterviewerServiceImpl implements InterviewerService {

    private final InterviewerRepository interviewerRepository;

    @Override
    @Transactional
    public InterviewerResponse createInterviewer(CreateInterviewerRequest request) {
        log.info("Creating interviewer with email: {}", request.getEmail());

        if (interviewerRepository.existsByEmail(request.getEmail())) {
            throw new ValidationException("email", "An interviewer with this email already exists");
        }

        Interviewer interviewer = Interviewer.builder()
                .name(request.getName())
                .email(request.getEmail())
                .maxInterviewsPerWeek(request.getMaxInterviewsPerWeek())
                .slotDurationMinutes(request.getSlotDurationMinutes() != null ? request.getSlotDurationMinutes() : 60)
                .createdAt(LocalDateTime.now())
                .build();

        interviewer = interviewerRepository.save(interviewer);

        if (request.getWeeklyAvailabilities() != null && !request.getWeeklyAvailabilities().isEmpty()) {
            for (WeeklyAvailabilityRequest availReq : request.getWeeklyAvailabilities()) {
                validateAvailability(availReq);

                WeeklyAvailability availability = WeeklyAvailability.builder()
                        .interviewer(interviewer)
                        .dayOfWeek(availReq.getDayOfWeek())
                        .startTime(availReq.getStartTime())
                        .endTime(availReq.getEndTime())
                        .isActive(true)
                        .build();

                interviewer.addWeeklyAvailability(availability);
            }
            interviewer = interviewerRepository.save(interviewer);
        }

        log.info("Created interviewer with ID: {}", interviewer.getId());
        return mapToResponse(interviewer);
    }

    @Override
    @Transactional(readOnly = true)
    public InterviewerResponse getInterviewerById(Long id) {
        Interviewer interviewer = interviewerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Interviewer", "id", id));
        return mapToResponse(interviewer);
    }

    @Override
    @Transactional(readOnly = true)
    public List<InterviewerResponse> getAllInterviewers() {
        return interviewerRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public InterviewerResponse updateWeeklyAvailability(Long interviewerId,
            List<WeeklyAvailabilityRequest> availabilities) {
        log.info("Updating weekly availability for interviewer: {}", interviewerId);

        Interviewer interviewer = interviewerRepository.findById(interviewerId)
                .orElseThrow(() -> new ResourceNotFoundException("Interviewer", "id", interviewerId));

        interviewer.getWeeklyAvailabilities().clear();

        if (availabilities != null) {
            for (WeeklyAvailabilityRequest availReq : availabilities) {
                validateAvailability(availReq);

                WeeklyAvailability availability = WeeklyAvailability.builder()
                        .interviewer(interviewer)
                        .dayOfWeek(availReq.getDayOfWeek())
                        .startTime(availReq.getStartTime())
                        .endTime(availReq.getEndTime())
                        .isActive(true)
                        .build();

                interviewer.addWeeklyAvailability(availability);
            }
        }

        interviewer.setUpdatedAt(LocalDateTime.now());
        interviewer = interviewerRepository.save(interviewer);

        log.info("Updated weekly availability for interviewer: {}", interviewerId);
        return mapToResponse(interviewer);
    }

    @Override
    @Transactional
    public InterviewerResponse updateMaxInterviewsPerWeek(Long interviewerId, Integer maxInterviews) {
        log.info("Updating max interviews per week for interviewer: {} to {}", interviewerId, maxInterviews);

        if (maxInterviews < 1) {
            throw new ValidationException("maxInterviewsPerWeek", "Must be at least 1");
        }

        Interviewer interviewer = interviewerRepository.findById(interviewerId)
                .orElseThrow(() -> new ResourceNotFoundException("Interviewer", "id", interviewerId));

        interviewer.setMaxInterviewsPerWeek(maxInterviews);
        interviewer.setUpdatedAt(LocalDateTime.now());
        interviewer = interviewerRepository.save(interviewer);

        return mapToResponse(interviewer);
    }

    @Override
    @Transactional
    public void deleteInterviewer(Long id) {
        log.info("Deleting interviewer: {}", id);

        if (!interviewerRepository.existsById(id)) {
            throw new ResourceNotFoundException("Interviewer", "id", id);
        }

        interviewerRepository.deleteById(id);
        log.info("Deleted interviewer: {}", id);
    }

    private void validateAvailability(WeeklyAvailabilityRequest request) {
        if (request.getEndTime() != null && request.getStartTime() != null
                && !request.getEndTime().isAfter(request.getStartTime())) {
            throw new ValidationException("endTime", "End time must be after start time");
        }
    }

    private InterviewerResponse mapToResponse(Interviewer interviewer) {
        List<WeeklyAvailabilityResponse> availabilities = interviewer.getWeeklyAvailabilities()
                .stream()
                .map(wa -> WeeklyAvailabilityResponse.builder()
                        .id(wa.getId())
                        .dayOfWeek(wa.getDayOfWeek())
                        .startTime(wa.getStartTime())
                        .endTime(wa.getEndTime())
                        .isActive(wa.getIsActive())
                        .build())
                .collect(Collectors.toList());

        return InterviewerResponse.builder()
                .id(interviewer.getId())
                .name(interviewer.getName())
                .email(interviewer.getEmail())
                .maxInterviewsPerWeek(interviewer.getMaxInterviewsPerWeek())
                .slotDurationMinutes(interviewer.getSlotDurationMinutes())
                .weeklyAvailabilities(availabilities)
                .createdAt(interviewer.getCreatedAt())
                .build();
    }
}
