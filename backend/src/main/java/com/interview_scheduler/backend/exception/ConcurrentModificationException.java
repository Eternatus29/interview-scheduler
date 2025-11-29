package com.interview_scheduler.backend.exception;

import org.springframework.http.HttpStatus;

public class ConcurrentModificationException extends BaseException {

    public ConcurrentModificationException(String resourceType) {
        super(
                String.format("The %s was modified by another request. Please retry.", resourceType),
                HttpStatus.CONFLICT,
                "CONCURRENT_MODIFICATION");
    }

    public ConcurrentModificationException(String message, Throwable cause) {
        super(message, cause, HttpStatus.CONFLICT, "CONCURRENT_MODIFICATION");
    }
}
