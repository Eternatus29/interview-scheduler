package com.interview_scheduler.backend.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.interview_scheduler.backend.dto.request.GenerateSlotsRequest;
import com.interview_scheduler.backend.dto.response.InterviewSlotResponse;
import com.interview_scheduler.backend.dto.response.PaginatedResponse;
import com.interview_scheduler.backend.entity.SlotStatus;
import com.interview_scheduler.backend.exception.ResourceNotFoundException;
import com.interview_scheduler.backend.exception.ValidationException;
import com.interview_scheduler.backend.service.SlotService;

@WebMvcTest(SlotController.class)
@DisplayName("Slot Controller Tests")
class SlotControllerTest {

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private ObjectMapper objectMapper;

        @MockitoBean
        private SlotService slotService;

        private InterviewSlotResponse testSlotResponse;
        private List<InterviewSlotResponse> testSlotList;

        @BeforeEach
        void setUp() {
                testSlotResponse = InterviewSlotResponse.builder()
                                .id(1L)
                                .interviewerId(1L)
                                .interviewerName("Test Interviewer")
                                .startTime(LocalDateTime.now().plusDays(1))
                                .endTime(LocalDateTime.now().plusDays(1).plusHours(1))
                                .status(SlotStatus.AVAILABLE)
                                .weekNumber(1)
                                .year(2025)
                                .build();

                InterviewSlotResponse slot2 = InterviewSlotResponse.builder()
                                .id(2L)
                                .interviewerId(1L)
                                .interviewerName("Test Interviewer")
                                .startTime(LocalDateTime.now().plusDays(2))
                                .endTime(LocalDateTime.now().plusDays(2).plusHours(1))
                                .status(SlotStatus.AVAILABLE)
                                .weekNumber(1)
                                .year(2025)
                                .build();

                testSlotList = Arrays.asList(testSlotResponse, slot2);
        }

        @Nested
        @DisplayName("Health Check Tests")
        class HealthCheckTests {

                @Test
                @DisplayName("GET /api/slots/health - Success")
                void healthCheck_Success() throws Exception {
                        mockMvc.perform(get("/api/slots/health"))
                                        .andExpect(status().isOk())
                                        .andExpect(jsonPath("$.success").value(true))
                                        .andExpect(jsonPath("$.data").value("Interview Scheduler API is running"));
                }
        }

        @Nested
        @DisplayName("Generate Slots Tests")
        class GenerateSlotsTests {

                @Test
                @DisplayName("POST /api/slots/generate - Success")
                void generateSlots_Success() throws Exception {
                        GenerateSlotsRequest request = GenerateSlotsRequest.builder()
                                        .interviewerId(1L)
                                        .weeksToGenerate(2)
                                        .build();

                        when(slotService.generateSlots(any(GenerateSlotsRequest.class)))
                                        .thenReturn(testSlotList);

                        mockMvc.perform(post("/api/slots/generate")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(request)))
                                        .andExpect(status().isOk())
                                        .andExpect(jsonPath("$.success").value(true))
                                        .andExpect(jsonPath("$.message").value("Generated 2 slots successfully"))
                                        .andExpect(jsonPath("$.data").isArray())
                                        .andExpect(jsonPath("$.data[0].id").value(1));
                }

                @Test
                @DisplayName("POST /api/slots/generate - Interviewer Not Found")
                void generateSlots_InterviewerNotFound() throws Exception {
                        GenerateSlotsRequest request = GenerateSlotsRequest.builder()
                                        .interviewerId(999L)
                                        .build();

                        when(slotService.generateSlots(any(GenerateSlotsRequest.class)))
                                        .thenThrow(new ResourceNotFoundException("Interviewer", "id", 999L));

                        mockMvc.perform(post("/api/slots/generate")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(request)))
                                        .andExpect(status().isNotFound())
                                        .andExpect(jsonPath("$.errorCode").value("RESOURCE_NOT_FOUND"));
                }

                @Test
                @DisplayName("POST /api/slots/generate - No Availability")
                void generateSlots_NoAvailability() throws Exception {
                        GenerateSlotsRequest request = GenerateSlotsRequest.builder()
                                        .interviewerId(1L)
                                        .build();

                        when(slotService.generateSlots(any(GenerateSlotsRequest.class)))
                                        .thenThrow(new ValidationException(
                                                        "No active weekly availability defined for this interviewer"));

                        mockMvc.perform(post("/api/slots/generate")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(request)))
                                        .andExpect(status().isBadRequest())
                                        .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"));
                }
        }

        @Nested
        @DisplayName("Get Available Slots Tests")
        class GetAvailableSlotsTests {

                @Test
                @DisplayName("GET /api/slots/available - Offset Pagination")
                void getAvailableSlots_OffsetPagination() throws Exception {
                        PaginatedResponse<InterviewSlotResponse> response = PaginatedResponse.ofOffset(
                                        testSlotList, 0, 10, 2L, 1);

                        when(slotService.getAvailableSlots(anyInt(), anyInt())).thenReturn(response);

                        mockMvc.perform(get("/api/slots/available")
                                        .param("page", "0")
                                        .param("size", "10"))
                                        .andExpect(status().isOk())
                                        .andExpect(jsonPath("$.success").value(true))
                                        .andExpect(jsonPath("$.data.data").isArray())
                                        .andExpect(jsonPath("$.data.page").value(0))
                                        .andExpect(jsonPath("$.data.size").value(10))
                                        .andExpect(jsonPath("$.data.totalElements").value(2));
                }

                @Test
                @DisplayName("GET /api/slots/available/cursor - Cursor Pagination")
                void getAvailableSlots_CursorPagination() throws Exception {
                        PaginatedResponse<InterviewSlotResponse> response = PaginatedResponse.ofCursor(
                                        testSlotList, 2L, 0L, false);

                        when(slotService.getAvailableSlotsByCursor(anyLong(), anyInt())).thenReturn(response);

                        mockMvc.perform(get("/api/slots/available/cursor")
                                        .param("cursor", "0")
                                        .param("limit", "10"))
                                        .andExpect(status().isOk())
                                        .andExpect(jsonPath("$.success").value(true))
                                        .andExpect(jsonPath("$.data.data").isArray())
                                        .andExpect(jsonPath("$.data.nextCursor").value(2))
                                        .andExpect(jsonPath("$.data.hasNext").value(false));
                }
        }

        @Nested
        @DisplayName("Get Slot By ID Tests")
        class GetSlotByIdTests {

                @Test
                @DisplayName("GET /api/slots/{id} - Success")
                void getSlotById_Success() throws Exception {
                        when(slotService.getSlotById(1L)).thenReturn(testSlotResponse);

                        mockMvc.perform(get("/api/slots/1"))
                                        .andExpect(status().isOk())
                                        .andExpect(jsonPath("$.success").value(true))
                                        .andExpect(jsonPath("$.data.id").value(1));
                }

                @Test
                @DisplayName("GET /api/slots/{id} - Not Found")
                void getSlotById_NotFound() throws Exception {
                        when(slotService.getSlotById(999L))
                                        .thenThrow(new ResourceNotFoundException("Interview Slot", "id", 999L));

                        mockMvc.perform(get("/api/slots/999"))
                                        .andExpect(status().isNotFound())
                                        .andExpect(jsonPath("$.errorCode").value("RESOURCE_NOT_FOUND"));
                }
        }

        @Nested
        @DisplayName("Get Slots For Interviewer Tests")
        class GetSlotsForInterviewerTests {

                @Test
                @DisplayName("GET /api/slots/available/interviewer/{id} - Success")
                void getAvailableSlotsForInterviewer_Success() throws Exception {
                        when(slotService.getAvailableSlotsForInterviewer(1L)).thenReturn(testSlotList);

                        mockMvc.perform(get("/api/slots/available/interviewer/1"))
                                        .andExpect(status().isOk())
                                        .andExpect(jsonPath("$.success").value(true))
                                        .andExpect(jsonPath("$.data").isArray())
                                        .andExpect(jsonPath("$.data.length()").value(2));
                }
        }
}
