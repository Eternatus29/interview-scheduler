package com.interview_scheduler.backend.config;

import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.interview_scheduler.backend.entity.GeneratedSlot;
import com.interview_scheduler.backend.entity.InterviewerSlot;
import com.interview_scheduler.backend.entity.SlotStatus;
import com.interview_scheduler.backend.repository.GeneratedSlotRepository;
import com.interview_scheduler.backend.repository.InterviewerSlotRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class DataLoader implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataLoader.class);

    private final InterviewerSlotRepository interviewerSlotRepo;
    private final GeneratedSlotRepository generatedSlotRepo;

    @Override
    public void run(String... args) throws Exception {
        if (interviewerSlotRepo.count() > 0 || generatedSlotRepo.count() > 0) {
            log.info("DataLoader: DB already contains data, skipping seeding.");
            return;
        }

        log.info("DataLoader: Seeding sample data...");

        InterviewerSlot is = new InterviewerSlot();
        is.setInterviewerId(1001L);
        is.setStartTime(LocalDateTime.now().plusDays(1).withHour(9).withMinute(0).withSecond(0).withNano(0));
        is.setEndTime(is.getStartTime().plusMinutes(30));
        is.setBooked(false);
        is.setWeekNumber(is.getStartTime().get(java.time.temporal.IsoFields.WEEK_OF_WEEK_BASED_YEAR));
        interviewerSlotRepo.save(is);

        GeneratedSlot gs = new GeneratedSlot();
        gs.setInterviewerSlot(is);
        gs.setSlotTime(is.getStartTime());
        gs.setStatus(SlotStatus.AVAILABLE);
        generatedSlotRepo.save(gs);

        log.info("DataLoader: Seeded 1 InterviewerSlot and 1 GeneratedSlot (interviewerId={}).", is.getInterviewerId());
    }
}
