package com.interview_scheduler.backend.controller;

import java.util.List;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.interview_scheduler.backend.dto.request.GenerateSlotsRequest;
import com.interview_scheduler.backend.dto.response.ApiResponse;
import com.interview_scheduler.backend.dto.response.InterviewSlotResponse;
import com.interview_scheduler.backend.dto.response.PaginatedResponse;
import com.interview_scheduler.backend.service.SlotService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/slots")
@RequiredArgsConstructor
@Slf4j
public class SlotController {

    private final SlotService slotService;

    @GetMapping("/health")
    public ResponseEntity<ApiResponse<String>> healthCheck() {
        return ResponseEntity.ok(ApiResponse.success("Interview Scheduler API is running"));
    }

    @PostMapping("/generate")
    public ResponseEntity<ApiResponse<List<InterviewSlotResponse>>> generateSlots(
            @Valid @RequestBody GenerateSlotsRequest request) {
        log.info("Generating slots for interviewer: {}", request.getInterviewerId());
        List<InterviewSlotResponse> slots = slotService.generateSlots(request);
        return ResponseEntity.ok(ApiResponse.success(
                String.format("Generated %d slots successfully", slots.size()), slots));
    }

    @GetMapping("/available")
    public ResponseEntity<ApiResponse<PaginatedResponse<InterviewSlotResponse>>> getAvailableSlots(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        PaginatedResponse<InterviewSlotResponse> response = slotService.getAvailableSlots(page, size);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/available/cursor")
    public ResponseEntity<ApiResponse<PaginatedResponse<InterviewSlotResponse>>> getAvailableSlotsByCursor(
            @RequestParam(defaultValue = "0") Long cursor,
            @RequestParam(defaultValue = "10") int limit) {
        PaginatedResponse<InterviewSlotResponse> response = slotService.getAvailableSlotsByCursor(cursor, limit);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/available/interviewer/{interviewerId}")
    public ResponseEntity<ApiResponse<List<InterviewSlotResponse>>> getAvailableSlotsForInterviewer(
            @PathVariable Long interviewerId) {
        List<InterviewSlotResponse> slots = slotService.getAvailableSlotsForInterviewer(interviewerId);
        return ResponseEntity.ok(ApiResponse.success(slots));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<InterviewSlotResponse>> getSlotById(@PathVariable Long id) {
        InterviewSlotResponse response = slotService.getSlotById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
