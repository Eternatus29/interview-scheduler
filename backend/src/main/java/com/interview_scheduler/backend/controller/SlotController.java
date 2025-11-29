package com.interview_scheduler.backend.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.interview_scheduler.backend.entity.GeneratedSlot;
import com.interview_scheduler.backend.repository.GeneratedSlotRepository;
import com.interview_scheduler.backend.service.SlotBookingService;
import com.interview_scheduler.backend.service.SlotGenerationService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/slots")
@RequiredArgsConstructor
public class SlotController {

    private final SlotGenerationService slotGenerationService;
    private final SlotBookingService slotBookingService;
    private final GeneratedSlotRepository generatedSlotRepo;

    @GetMapping("/healthcheck")
    public String healthCheck() {
        return "API is running";
    }

    @PostMapping("/generate/{interviewerId}")
    public String generate(@PathVariable Long interviewerId) {
        slotGenerationService.generateSlotsForNextTwoWeeks(interviewerId);
        return "Slots generated";
    }

    @GetMapping
    public Page<GeneratedSlot> getSlots(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return generatedSlotRepo.findAll(PageRequest.of(page, size));
    }

    @PostMapping("/book")
    public String bookSlot(@RequestParam Long slotId, @RequestParam Long candidateId) {
        return slotBookingService.bookSlot(slotId, candidateId);
    }

    @PutMapping("/update")
    public String updateSlot(@RequestParam Long slotId, @RequestParam Long candidateId) {
        return slotBookingService.updateSlot(slotId, candidateId);
    }

    @GetMapping("/cursor")
    public List<GeneratedSlot> cursorPagination(@RequestParam Long cursor, @RequestParam int limit) {
        return generatedSlotRepo.findAll().stream()
                .filter(s -> s.getId() > cursor)
                .limit(limit)
                .collect(Collectors.toList());
    }

}
