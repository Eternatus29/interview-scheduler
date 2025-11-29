package com.interview_scheduler.backend.integration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.interview_scheduler.backend.dto.request.BookSlotRequest;
import com.interview_scheduler.backend.dto.request.CreateCandidateRequest;
import com.interview_scheduler.backend.dto.request.CreateInterviewerRequest;
import com.interview_scheduler.backend.dto.request.GenerateSlotsRequest;
import com.interview_scheduler.backend.dto.request.WeeklyAvailabilityRequest;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(locations = "classpath:application.properties")
@TestMethodOrder(OrderAnnotation.class)
@DisplayName("Interview Scheduling Integration Tests")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class InterviewSchedulingIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private static Long interviewerId;
    private static Long candidateId;
    private static Long slotId;
    private static Long bookingId;

    private Long setupInterviewer() throws Exception {
        List<WeeklyAvailabilityRequest> availabilities = Arrays.asList(
                WeeklyAvailabilityRequest.builder()
                        .dayOfWeek(DayOfWeek.MONDAY)
                        .startTime(LocalTime.of(9, 0))
                        .endTime(LocalTime.of(17, 0))
                        .build(),
                WeeklyAvailabilityRequest.builder()
                        .dayOfWeek(DayOfWeek.WEDNESDAY)
                        .startTime(LocalTime.of(10, 0))
                        .endTime(LocalTime.of(16, 0))
                        .build());

        CreateInterviewerRequest request = CreateInterviewerRequest.builder()
                .name("Integration Test Interviewer")
                .email("integration.test" + System.currentTimeMillis() + "@company.com")
                .maxInterviewsPerWeek(10)
                .slotDurationMinutes(60)
                .weeklyAvailabilities(availabilities)
                .build();

        MvcResult result = mockMvc.perform(post("/api/interviewers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();

        JsonNode responseJson = objectMapper.readTree(result.getResponse().getContentAsString());
        return responseJson.path("data").path("id").asLong();
    }

    private Long setupCandidate() throws Exception {
        CreateCandidateRequest request = CreateCandidateRequest.builder()
                .name("Integration Test Candidate")
                .email("candidate.test" + System.currentTimeMillis() + "@email.com")
                .phoneNumber("+1-555-1234")
                .build();

        MvcResult result = mockMvc.perform(post("/api/candidates")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();

        JsonNode responseJson = objectMapper.readTree(result.getResponse().getContentAsString());
        return responseJson.path("data").path("id").asLong();
    }

    private Long setupSlots(Long interviewerId) throws Exception {
        GenerateSlotsRequest request = GenerateSlotsRequest.builder()
                .interviewerId(interviewerId)
                .weeksToGenerate(2)
                .build();

        MvcResult result = mockMvc.perform(post("/api/slots/generate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode responseJson = objectMapper.readTree(result.getResponse().getContentAsString());
        if (responseJson.path("data").size() > 0) {
            return responseJson.path("data").get(0).path("id").asLong();
        }
        return null;
    }

    @Test
    @Order(1)
    @DisplayName("1. Create an interviewer with weekly availability")
    void createInterviewer() throws Exception {
        List<WeeklyAvailabilityRequest> availabilities = Arrays.asList(
                WeeklyAvailabilityRequest.builder()
                        .dayOfWeek(DayOfWeek.MONDAY)
                        .startTime(LocalTime.of(9, 0))
                        .endTime(LocalTime.of(17, 0))
                        .build(),
                WeeklyAvailabilityRequest.builder()
                        .dayOfWeek(DayOfWeek.WEDNESDAY)
                        .startTime(LocalTime.of(10, 0))
                        .endTime(LocalTime.of(16, 0))
                        .build());

        CreateInterviewerRequest request = CreateInterviewerRequest.builder()
                .name("Integration Test Interviewer")
                .email("integration.test@company.com")
                .maxInterviewsPerWeek(10)
                .slotDurationMinutes(60)
                .weeklyAvailabilities(availabilities)
                .build();

        MvcResult result = mockMvc.perform(post("/api/interviewers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.name").value("Integration Test Interviewer"))
                .andExpect(jsonPath("$.data.email").value("integration.test@company.com"))
                .andExpect(jsonPath("$.data.maxInterviewsPerWeek").value(10))
                .andReturn();

        JsonNode responseJson = objectMapper.readTree(result.getResponse().getContentAsString());
        interviewerId = responseJson.path("data").path("id").asLong();
    }

    @Test
    @Order(2)
    @DisplayName("2. Create a candidate")
    void createCandidate() throws Exception {
        CreateCandidateRequest request = CreateCandidateRequest.builder()
                .name("Integration Test Candidate")
                .email("candidate.test@email.com")
                .phoneNumber("+1-555-1234")
                .build();

        MvcResult result = mockMvc.perform(post("/api/candidates")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.name").value("Integration Test Candidate"))
                .andReturn();

        JsonNode responseJson = objectMapper.readTree(result.getResponse().getContentAsString());
        candidateId = responseJson.path("data").path("id").asLong();
    }

    @Test
    @Order(3)
    @DisplayName("3. Generate interview slots for the next 2 weeks")
    void generateSlots() throws Exception {
        if (interviewerId == null) {
            interviewerId = setupInterviewer();
        }

        GenerateSlotsRequest request = GenerateSlotsRequest.builder()
                .interviewerId(interviewerId)
                .weeksToGenerate(2)
                .build();

        MvcResult result = mockMvc.perform(post("/api/slots/generate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andReturn();

        JsonNode responseJson = objectMapper.readTree(result.getResponse().getContentAsString());
        if (responseJson.path("data").size() > 0) {
            slotId = responseJson.path("data").get(0).path("id").asLong();
        }
    }

    @Test
    @Order(4)
    @DisplayName("4. Get available slots with offset pagination")
    void getAvailableSlotsOffset() throws Exception {
        mockMvc.perform(get("/api/slots/available")
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.data").isArray())
                .andExpect(jsonPath("$.data.page").value(0));
    }

    @Test
    @Order(5)
    @DisplayName("5. Get available slots with cursor pagination")
    void getAvailableSlotsCursor() throws Exception {
        mockMvc.perform(get("/api/slots/available/cursor")
                .param("cursor", "0")
                .param("limit", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.data").isArray());
    }

    @Test
    @Order(6)
    @DisplayName("6. Book a slot for the candidate")
    void bookSlot() throws Exception {
        if (interviewerId == null) {
            interviewerId = setupInterviewer();
        }
        if (slotId == null) {
            slotId = setupSlots(interviewerId);
        }
        if (candidateId == null) {
            candidateId = setupCandidate();
        }

        if (slotId == null) {
            return;
        }

        BookSlotRequest request = BookSlotRequest.builder()
                .slotId(slotId)
                .candidateId(candidateId)
                .bookingNotes("Integration test booking")
                .build();

        MvcResult result = mockMvc.perform(post("/api/bookings")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Slot booked successfully"))
                .andExpect(jsonPath("$.data.candidateId").value(candidateId))
                .andExpect(jsonPath("$.data.slotId").value(slotId))
                .andExpect(jsonPath("$.data.status").value("PENDING"))
                .andReturn();

        JsonNode responseJson = objectMapper.readTree(result.getResponse().getContentAsString());
        bookingId = responseJson.path("data").path("id").asLong();
    }

    @Test
    @Order(7)
    @DisplayName("7. Confirm the booking")
    void confirmBooking() throws Exception {
        if (bookingId == null) {
            bookSlot();
        }

        if (bookingId == null) {
            return;
        }

        mockMvc.perform(post("/api/bookings/" + bookingId + "/confirm"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("CONFIRMED"));
    }

    @Test
    @Order(8)
    @DisplayName("8. Get booking details")
    void getBookingDetails() throws Exception {
        if (bookingId == null) {
            return;
        }

        mockMvc.perform(get("/api/bookings/" + bookingId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(bookingId));
    }

    @Test
    @Order(9)
    @DisplayName("9. Get bookings by candidate")
    void getBookingsByCandidate() throws Exception {
        if (candidateId == null) {
            candidateId = setupCandidate();
        }

        mockMvc.perform(get("/api/bookings/candidate/" + candidateId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    @Order(10)
    @DisplayName("10. Health check endpoint")
    void healthCheck() throws Exception {
        mockMvc.perform(get("/api/slots/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").value("Interview Scheduler API is running"));
    }
}
