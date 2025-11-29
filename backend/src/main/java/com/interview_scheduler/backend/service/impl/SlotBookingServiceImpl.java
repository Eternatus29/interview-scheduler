package com.interview_scheduler.backend.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.interview_scheduler.backend.entity.GeneratedSlot;
import com.interview_scheduler.backend.entity.SlotStatus;
import com.interview_scheduler.backend.repository.GeneratedSlotRepository;
import com.interview_scheduler.backend.service.SlotBookingService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SlotBookingServiceImpl implements SlotBookingService {

    private final GeneratedSlotRepository generatedSlotRepo;

    @Override
    @Transactional
    public String bookSlot(Long slotId, Long candidateId) {
        GeneratedSlot slot = generatedSlotRepo.findById(slotId)
                .orElseThrow(() -> new RuntimeException("Slot not found"));

        if (slot.getStatus() == SlotStatus.BOOKED)
            throw new RuntimeException("Slot already booked");

        slot.setStatus(SlotStatus.BOOKED);
        slot.setCandidateId(candidateId);

        generatedSlotRepo.save(slot);
        return "Slot booked successfully";
    }

    @Override
    @Transactional
    public String updateSlot(Long slotId, Long candidateId) {
        return bookSlot(slotId, candidateId);
    }
}
