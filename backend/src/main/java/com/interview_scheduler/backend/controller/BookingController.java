package com.interview_scheduler.backend.controller;

import java.util.List;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.interview_scheduler.backend.dto.request.BookSlotRequest;
import com.interview_scheduler.backend.dto.request.UpdateBookingRequest;
import com.interview_scheduler.backend.dto.response.ApiResponse;
import com.interview_scheduler.backend.dto.response.BookingResponse;
import com.interview_scheduler.backend.service.BookingService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
@Slf4j
public class BookingController {

    private final BookingService bookingService;

    @PostMapping
    public ResponseEntity<ApiResponse<BookingResponse>> bookSlot(
            @Valid @RequestBody BookSlotRequest request) {
        log.info("Booking slot {} for candidate {}", request.getSlotId(), request.getCandidateId());
        BookingResponse response = bookingService.bookSlot(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Slot booked successfully", response));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<BookingResponse>> updateBooking(
            @PathVariable Long id,
            @Valid @RequestBody UpdateBookingRequest request) {
        log.info("Updating booking {} to new slot {}", id, request.getNewSlotId());
        BookingResponse response = bookingService.updateBooking(id, request);
        return ResponseEntity.ok(ApiResponse.success("Booking updated successfully", response));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<BookingResponse>> cancelBooking(@PathVariable Long id) {
        log.info("Cancelling booking {}", id);
        BookingResponse response = bookingService.cancelBooking(id);
        return ResponseEntity.ok(ApiResponse.success("Booking cancelled successfully", response));
    }

    @PostMapping("/{id}/confirm")
    public ResponseEntity<ApiResponse<BookingResponse>> confirmBooking(@PathVariable Long id) {
        log.info("Confirming booking {}", id);
        BookingResponse response = bookingService.confirmBooking(id);
        return ResponseEntity.ok(ApiResponse.success("Booking confirmed successfully", response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<BookingResponse>> getBookingById(@PathVariable Long id) {
        BookingResponse response = bookingService.getBookingById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/candidate/{candidateId}")
    public ResponseEntity<ApiResponse<List<BookingResponse>>> getBookingsByCandidate(
            @PathVariable Long candidateId) {
        List<BookingResponse> bookings = bookingService.getBookingsByCandidateId(candidateId);
        return ResponseEntity.ok(ApiResponse.success(bookings));
    }

    @GetMapping("/slot/{slotId}")
    public ResponseEntity<ApiResponse<BookingResponse>> getBookingBySlot(@PathVariable Long slotId) {
        BookingResponse response = bookingService.getBookingBySlotId(slotId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
