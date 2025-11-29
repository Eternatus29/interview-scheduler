package com.interview_scheduler.backend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import com.interview_scheduler.backend.service.SlotService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Configuration
@EnableScheduling
@RequiredArgsConstructor
@Slf4j
public class SchedulingConfig {

    private final SlotService slotService;

    @Scheduled(fixedRate = 3600000)
    public void markExpiredSlots() {
        log.info("Running scheduled task: markExpiredSlots");
        int count = slotService.markExpiredSlots();
        if (count > 0) {
            log.info("Marked {} slots as expired", count);
        }
    }
}
