package com.interview_scheduler.backend.controller;

import static org.mockito.ArgumentMatchers.any;
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
import com.interview_scheduler.backend.dto.request.BookSlotRequest;
import com.interview_scheduler.backend.dto.request.UpdateBookingRequest;
import com.interview_scheduler.backend.dto.response.BookingResponse;
import com.interview_scheduler.backend.entity.BookingStatus;
import com.interview_scheduler.backend.exception.DuplicateBookingException;
import com.interview_scheduler.backend.exception.ResourceNotFoundException;
import com.interview_scheduler.backend.exception.SlotAlreadyBookedException;
import com.interview_scheduler.backend.service.BookingService;

@WebMvcTest(BookingController.class)
@DisplayName("Booking Controller Tests")
class BookingControllerTest {

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private ObjectMapper objectMapper;

        @MockitoBean
        private BookingService bookingService;

        private BookingResponse testBookingResponse;

        @BeforeEach
        void setUp() {
                testBookingResponse = BookingResponse.builder()
                                .id(1L)
                                .candidateId(1L)
                                .candidateName("Test Candidate")
                                .candidateEmail("candidate@test.com")
                                .slotId(1L)
                                .slotStartTime(LocalDateTime.now().plusDays(1))
                                .slotEndTime(LocalDateTime.now().plusDays(1).plusHours(1))
                                .interviewerId(1L)
                                .interviewerName("Test Interviewer")
                                .status(BookingStatus.PENDING)
                                .weekNumber(1)
                                .year(2025)
                                .createdAt(LocalDateTime.now())
                                .build();
        }

        @Nested
        @DisplayName("Book Slot Endpoint Tests")
        class BookSlotTests {

                @Test
                @DisplayName("POST /api/bookings - Success")
                void bookSlot_Success() throws Exception {
                        BookSlotRequest request = BookSlotRequest.builder()
                                        .slotId(1L)
                                        .candidateId(1L)
                                        .bookingNotes("Test booking")
                                        .build();

                        when(bookingService.bookSlot(any(BookSlotRequest.class)))
                                        .thenReturn(testBookingResponse);

                        mockMvc.perform(post("/api/bookings")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(request)))
                                        .andExpect(status().isCreated())
                                        .andExpect(jsonPath("$.success").value(true))
                                        .andExpect(jsonPath("$.message").value("Slot booked successfully"))
                                        .andExpect(jsonPath("$.data.id").value(1))
                                        .andExpect(jsonPath("$.data.candidateId").value(1))
                                        .andExpect(jsonPath("$.data.slotId").value(1));
                }

                @Test
                @DisplayName("POST /api/bookings - Slot Already Booked")
                void bookSlot_SlotAlreadyBooked() throws Exception {
                        BookSlotRequest request = BookSlotRequest.builder()
                                        .slotId(1L)
                                        .candidateId(1L)
                                        .build();

                        when(bookingService.bookSlot(any(BookSlotRequest.class)))
                                        .thenThrow(new SlotAlreadyBookedException(1L));

                        mockMvc.perform(post("/api/bookings")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(request)))
                                        .andExpect(status().isConflict())
                                        .andExpect(jsonPath("$.errorCode").value("SLOT_ALREADY_BOOKED"));
                }

                @Test
                @DisplayName("POST /api/bookings - Duplicate Booking")
                void bookSlot_DuplicateBooking() throws Exception {
                        BookSlotRequest request = BookSlotRequest.builder()
                                        .slotId(1L)
                                        .candidateId(1L)
                                        .build();

                        when(bookingService.bookSlot(any(BookSlotRequest.class)))
                                        .thenThrow(new DuplicateBookingException(1L));

                        mockMvc.perform(post("/api/bookings")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(request)))
                                        .andExpect(status().isConflict())
                                        .andExpect(jsonPath("$.errorCode").value("DUPLICATE_BOOKING"));
                }

                @Test
                @DisplayName("POST /api/bookings - Validation Error")
                void bookSlot_ValidationError() throws Exception {
                        String invalidRequest = "{}";

                        mockMvc.perform(post("/api/bookings")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(invalidRequest))
                                        .andExpect(status().isBadRequest())
                                        .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"));
                }
        }

        @Nested
        @DisplayName("Update Booking Endpoint Tests")
        class UpdateBookingTests {

                @Test
                @DisplayName("PUT /api/bookings/{id} - Success")
                void updateBooking_Success() throws Exception {
                        UpdateBookingRequest request = UpdateBookingRequest.builder()
                                        .newSlotId(2L)
                                        .bookingNotes("Rescheduled")
                                        .build();

                        when(bookingService.updateBooking(anyLong(), any(UpdateBookingRequest.class)))
                                        .thenReturn(testBookingResponse);

                        mockMvc.perform(put("/api/bookings/1")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(request)))
                                        .andExpect(status().isOk())
                                        .andExpect(jsonPath("$.success").value(true))
                                        .andExpect(jsonPath("$.message").value("Booking updated successfully"));
                }

                @Test
                @DisplayName("PUT /api/bookings/{id} - Not Found")
                void updateBooking_NotFound() throws Exception {
                        UpdateBookingRequest request = UpdateBookingRequest.builder()
                                        .newSlotId(2L)
                                        .build();

                        when(bookingService.updateBooking(anyLong(), any(UpdateBookingRequest.class)))
                                        .thenThrow(new ResourceNotFoundException("Booking", "id", 999L));

                        mockMvc.perform(put("/api/bookings/999")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(request)))
                                        .andExpect(status().isNotFound())
                                        .andExpect(jsonPath("$.errorCode").value("RESOURCE_NOT_FOUND"));
                }
        }

        @Nested
        @DisplayName("Cancel Booking Endpoint Tests")
        class CancelBookingTests {

                @Test
                @DisplayName("DELETE /api/bookings/{id} - Success")
                void cancelBooking_Success() throws Exception {
                        testBookingResponse.setStatus(BookingStatus.CANCELLED);
                        when(bookingService.cancelBooking(1L)).thenReturn(testBookingResponse);

                        mockMvc.perform(delete("/api/bookings/1"))
                                        .andExpect(status().isOk())
                                        .andExpect(jsonPath("$.success").value(true))
                                        .andExpect(jsonPath("$.message").value("Booking cancelled successfully"));
                }
        }

        @Nested
        @DisplayName("Confirm Booking Endpoint Tests")
        class ConfirmBookingTests {

                @Test
                @DisplayName("POST /api/bookings/{id}/confirm - Success")
                void confirmBooking_Success() throws Exception {
                        testBookingResponse.setStatus(BookingStatus.CONFIRMED);
                        when(bookingService.confirmBooking(1L)).thenReturn(testBookingResponse);

                        mockMvc.perform(post("/api/bookings/1/confirm"))
                                        .andExpect(status().isOk())
                                        .andExpect(jsonPath("$.success").value(true))
                                        .andExpect(jsonPath("$.message").value("Booking confirmed successfully"));
                }
        }

        @Nested
        @DisplayName("Get Booking Endpoint Tests")
        class GetBookingTests {

                @Test
                @DisplayName("GET /api/bookings/{id} - Success")
                void getBookingById_Success() throws Exception {
                        when(bookingService.getBookingById(1L)).thenReturn(testBookingResponse);

                        mockMvc.perform(get("/api/bookings/1"))
                                        .andExpect(status().isOk())
                                        .andExpect(jsonPath("$.success").value(true))
                                        .andExpect(jsonPath("$.data.id").value(1));
                }

                @Test
                @DisplayName("GET /api/bookings/candidate/{candidateId} - Success")
                void getBookingsByCandidate_Success() throws Exception {
                        List<BookingResponse> bookings = Arrays.asList(testBookingResponse);
                        when(bookingService.getBookingsByCandidateId(1L)).thenReturn(bookings);

                        mockMvc.perform(get("/api/bookings/candidate/1"))
                                        .andExpect(status().isOk())
                                        .andExpect(jsonPath("$.success").value(true))
                                        .andExpect(jsonPath("$.data").isArray())
                                        .andExpect(jsonPath("$.data[0].id").value(1));
                }
        }
}
