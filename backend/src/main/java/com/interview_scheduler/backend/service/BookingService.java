package com.interview_scheduler.backend.service;

import java.util.List;

import com.interview_scheduler.backend.dto.request.BookSlotRequest;
import com.interview_scheduler.backend.dto.request.UpdateBookingRequest;
import com.interview_scheduler.backend.dto.response.BookingResponse;

public interface BookingService {

    BookingResponse bookSlot(BookSlotRequest request);

    BookingResponse updateBooking(Long bookingId, UpdateBookingRequest request);

    BookingResponse cancelBooking(Long bookingId);

    BookingResponse confirmBooking(Long bookingId);

    BookingResponse getBookingById(Long id);

    List<BookingResponse> getBookingsByCandidateId(Long candidateId);

    BookingResponse getBookingBySlotId(Long slotId);
}
