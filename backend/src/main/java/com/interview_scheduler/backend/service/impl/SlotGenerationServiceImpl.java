package com.interview_scheduler.backend.service.impl;

import java.util.List;
import java.time.LocalDateTime;

import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.interview_scheduler.backend.entity.GeneratedSlot;
import com.interview_scheduler.backend.entity.SlotStatus;
import com.interview_scheduler.backend.entity.InterviewerSlot;
import com.interview_scheduler.backend.repository.GeneratedSlotRepository;
import com.interview_scheduler.backend.repository.InterviewerSlotRepository;
import com.interview_scheduler.backend.service.SlotGenerationService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor

public class SlotGenerationServiceImpl implements SlotGenerationService {

    private final InterviewerSlotRepository interviewerSlotRepo;
    private final GeneratedSlotRepository generatedSlotRepo;

    @Override
    @Transactional
    public void generateSlotsForNextTwoWeeks(Long interviewerId, List<LocalDateTime> slotTimes) {
        if (slotTimes != null && !slotTimes.isEmpty()) {
            // create slots for provided times
            // associate with any interviewerSlots that match the interviewerId (if any)
            List<InterviewerSlot> interviewerSlots = interviewerSlotRepo.findByInterviewerId(interviewerId);
            // pick first interviewerSlot as parent if exists
            InterviewerSlot parent = interviewerSlots.isEmpty() ? null : interviewerSlots.get(0);
            List<GeneratedSlot> toSave = slotTimes.stream().map(t -> {
                GeneratedSlot gs = new GeneratedSlot();
                gs.setInterviewerSlot(parent);
                gs.setSlotTime(t);
                gs.setStatus(SlotStatus.AVAILABLE);
                return gs;
            }).collect(Collectors.toList());
            generatedSlotRepo.saveAll(toSave);
            return;
        }

        // fallback to previous behavior: generate from InterviewerSlot start times
        List<InterviewerSlot> slots = interviewerSlotRepo.findByInterviewerId(interviewerId);
        for (InterviewerSlot is : slots) {
            GeneratedSlot gs = new GeneratedSlot();
            gs.setInterviewerSlot(is);
            gs.setSlotTime(is.getStartTime());
            gs.setStatus(SlotStatus.AVAILABLE);
            generatedSlotRepo.save(gs);
        }
    }
}
