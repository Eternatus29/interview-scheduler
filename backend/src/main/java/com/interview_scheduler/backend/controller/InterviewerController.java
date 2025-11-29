package com.interview_scheduler.backend.controller;

import java.util.List;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.interview_scheduler.backend.dto.request.CreateInterviewerRequest;
import com.interview_scheduler.backend.dto.request.WeeklyAvailabilityRequest;
import com.interview_scheduler.backend.dto.response.ApiResponse;
import com.interview_scheduler.backend.dto.response.InterviewerResponse;
import com.interview_scheduler.backend.service.InterviewerService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/interviewers")
@RequiredArgsConstructor
@Slf4j
public class InterviewerController {

    private final InterviewerService interviewerService;

    @PostMapping
    public ResponseEntity<ApiResponse<InterviewerResponse>> createInterviewer(
            @Valid @RequestBody CreateInterviewerRequest request) {
        log.info("Creating new interviewer: {}", request.getEmail());
        InterviewerResponse response = interviewerService.createInterviewer(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Interviewer created successfully", response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<InterviewerResponse>>> getAllInterviewers() {
        List<InterviewerResponse> interviewers = interviewerService.getAllInterviewers();
        return ResponseEntity.ok(ApiResponse.success(interviewers));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<InterviewerResponse>> getInterviewerById(@PathVariable Long id) {
        InterviewerResponse response = interviewerService.getInterviewerById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/{id}/availability")
    public ResponseEntity<ApiResponse<InterviewerResponse>> updateWeeklyAvailability(
            @PathVariable Long id,
            @Valid @RequestBody List<WeeklyAvailabilityRequest> availabilities) {
        log.info("Updating weekly availability for interviewer: {}", id);
        InterviewerResponse response = interviewerService.updateWeeklyAvailability(id, availabilities);
        return ResponseEntity.ok(ApiResponse.success("Weekly availability updated successfully", response));
    }

    @PatchMapping("/{id}/max-interviews")
    public ResponseEntity<ApiResponse<InterviewerResponse>> updateMaxInterviewsPerWeek(
            @PathVariable Long id,
            @RequestBody Integer maxInterviews) {
        log.info("Updating max interviews per week for interviewer: {} to {}", id, maxInterviews);
        InterviewerResponse response = interviewerService.updateMaxInterviewsPerWeek(id, maxInterviews);
        return ResponseEntity.ok(ApiResponse.success("Max interviews per week updated successfully", response));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteInterviewer(@PathVariable Long id) {
        log.info("Deleting interviewer: {}", id);
        interviewerService.deleteInterviewer(id);
        return ResponseEntity.ok(ApiResponse.success("Interviewer deleted successfully", null));
    }
}
