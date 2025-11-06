package com.example.springssodemo.exception;

/** Simple 409 conflict exception. */
public class ConflictException extends RuntimeException {
    public ConflictException(String message) { super(message); }
}
