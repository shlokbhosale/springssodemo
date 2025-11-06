package com.example.springssodemo.exception;

/** Simple 400 bad request exception. */
public class BadRequestException extends RuntimeException {
    public BadRequestException(String message) { super(message); }
}
