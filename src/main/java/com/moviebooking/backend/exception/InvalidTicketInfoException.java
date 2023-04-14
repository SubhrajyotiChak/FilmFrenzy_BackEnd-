package com.moviebooking.backend.exception;

public class InvalidTicketInfoException extends RuntimeException {
    public InvalidTicketInfoException(String message) {
        super(message);
    }
}
