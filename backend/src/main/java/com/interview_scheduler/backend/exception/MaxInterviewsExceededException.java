package com.interview_scheduler.backend.exception;

import org.springframework.http.HttpStatus;

public class MaxInterviewsExceededException extends BaseException {

    public MaxInterviewsExceededException(Long interviewerId, int maxAllowed, int weekNumber) {
        super(
                String.format("Interviewer %d has reached maximum interviews (%d) for week %d",
                        interviewerId, maxAllowed, weekNumber),
                HttpStatus.CONFLICT,
                "MAX_INTERVIEWS_EXCEEDED");
    }

    public MaxInterviewsExceededException(String message) {
        super(message, HttpStatus.CONFLICT, "MAX_INTERVIEWS_EXCEEDED");
    }
}
