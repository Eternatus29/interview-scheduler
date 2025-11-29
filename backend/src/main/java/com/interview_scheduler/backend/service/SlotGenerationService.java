package com.interview_scheduler.backend.service;

public interface SlotGenerationService {
    void generateSlotsForNextTwoWeeks(Long interviewerId);
}
