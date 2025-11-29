package com.interview_scheduler.backend.service;

import java.time.LocalDateTime;
import java.util.List;

public interface SlotGenerationService {
    /**
     * Generate slots for the interviewer. If slotTimes is non-empty, create
     * GeneratedSlot entries for those times. Otherwise fall back to existing
     * behavior.
     */
    void generateSlotsForNextTwoWeeks(Long interviewerId, List<LocalDateTime> slotTimes);
}
