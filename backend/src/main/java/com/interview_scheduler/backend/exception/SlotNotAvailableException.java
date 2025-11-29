package com.interview_scheduler.backend.exception;

import org.springframework.http.HttpStatus;

public class SlotNotAvailableException extends BaseException {

    public SlotNotAvailableException(Long slotId, String reason) {
        super(
                String.format("Slot with ID %d is not available: %s", slotId, reason),
                HttpStatus.BAD_REQUEST,
                "SLOT_NOT_AVAILABLE");
    }

    public SlotNotAvailableException(String message) {
        super(message, HttpStatus.BAD_REQUEST, "SLOT_NOT_AVAILABLE");
    }
}
