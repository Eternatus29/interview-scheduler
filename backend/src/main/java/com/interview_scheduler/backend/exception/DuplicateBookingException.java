package com.interview_scheduler.backend.exception;

import org.springframework.http.HttpStatus;

public class DuplicateBookingException extends BaseException {

    public DuplicateBookingException(Long candidateId) {
        super(
                String.format("Candidate with ID %d already has an active booking in this time period", candidateId),
                HttpStatus.CONFLICT,
                "DUPLICATE_BOOKING");
    }

    public DuplicateBookingException(String message) {
        super(message, HttpStatus.CONFLICT, "DUPLICATE_BOOKING");
    }
}
