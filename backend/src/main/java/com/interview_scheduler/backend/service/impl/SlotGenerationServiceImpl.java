package com.interview_scheduler.backend.service.impl;

import java.util.List;

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
    public void generateSlotsForNextTwoWeeks(Long interviewerId) {
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
