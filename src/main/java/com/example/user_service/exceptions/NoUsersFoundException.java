package com.example.user_service.exceptions;

public class NoUsersFoundException extends Exception {
    public NoUsersFoundException(String message) {
        super(message);
    }
}
