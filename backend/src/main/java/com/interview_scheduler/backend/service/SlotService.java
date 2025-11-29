package com.interview_scheduler.backend.service;

import java.util.List;

import com.interview_scheduler.backend.dto.request.GenerateSlotsRequest;
import com.interview_scheduler.backend.dto.response.InterviewSlotResponse;
import com.interview_scheduler.backend.dto.response.PaginatedResponse;

public interface SlotService {

    List<InterviewSlotResponse> generateSlots(GenerateSlotsRequest request);

    PaginatedResponse<InterviewSlotResponse> getAvailableSlots(int page, int size);

    PaginatedResponse<InterviewSlotResponse> getAvailableSlotsByCursor(Long cursor, int limit);

    List<InterviewSlotResponse> getAvailableSlotsForInterviewer(Long interviewerId);

    InterviewSlotResponse getSlotById(Long id);

    int markExpiredSlots();
}
