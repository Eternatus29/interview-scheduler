package com.interview_scheduler.backend.controller;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.web.servlet.MockMvc;

import com.interview_scheduler.backend.entity.GeneratedSlot;
import com.interview_scheduler.backend.entity.SlotStatus;
import com.interview_scheduler.backend.repository.GeneratedSlotRepository;
import com.interview_scheduler.backend.service.SlotBookingService;
import com.interview_scheduler.backend.service.SlotGenerationService;

@WebMvcTest(controllers = SlotController.class)
class SlotControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SlotGenerationService slotGenerationService;

    @MockitoBean
    private SlotBookingService slotBookingService;

    @MockitoBean
    private GeneratedSlotRepository generatedSlotRepo;

    @Test
    void generate_invokesService() throws Exception {
        mockMvc.perform(post("/api/slots/generate/1"))
                .andExpect(status().isOk())
                .andExpect(content().string("Slots generated"));

        verify(slotGenerationService).generateSlotsForNextTwoWeeks(1L);
    }

    @Test
    void getSlots_returnsPage() throws Exception {
        GeneratedSlot s = new GeneratedSlot();
        s.setId(1L);
        s.setSlotTime(LocalDateTime.now());
        s.setStatus(SlotStatus.AVAILABLE);

        when(generatedSlotRepo.findAll(PageRequest.of(0, 10))).thenReturn(new PageImpl<>(List.of(s)));

        mockMvc.perform(get("/api/slots"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1));
    }

    @Test
    void bookSlot_delegatesToService() throws Exception {
        when(slotBookingService.bookSlot(1L, 2L)).thenReturn("Slot booked successfully");

        mockMvc.perform(post("/api/slots/book").param("slotId", "1").param("candidateId", "2"))
                .andExpect(status().isOk())
                .andExpect(content().string("Slot booked successfully"));

        verify(slotBookingService).bookSlot(1L, 2L);
    }
}
