package com.example.springssodemo.exception;

/** Simple 404 exception. */
public class NotFoundException extends RuntimeException {
    public NotFoundException(String message) { super(message); }
}
