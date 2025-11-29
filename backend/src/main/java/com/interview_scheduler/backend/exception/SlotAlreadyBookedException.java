package com.interview_scheduler.backend.exception;

import org.springframework.http.HttpStatus;

public class SlotAlreadyBookedException extends BaseException {

    public SlotAlreadyBookedException(Long slotId) {
        super(
                String.format("Slot with ID %d is already booked", slotId),
                HttpStatus.CONFLICT,
                "SLOT_ALREADY_BOOKED");
    }

    public SlotAlreadyBookedException(String message) {
        super(message, HttpStatus.CONFLICT, "SLOT_ALREADY_BOOKED");
    }
}
