package com.interview_scheduler.backend.service;

public interface SlotBookingService {
    String bookSlot(Long slotId, Long candidateId);

    String updateSlot(Long slotId, Long candidateId);
}
