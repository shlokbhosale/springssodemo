package com.example.springssodemo.controller;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@ControllerAdvice
@RestController
public class ApiExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, Object> handleBadRequest(IllegalArgumentException ex){
        return Map.of("status", 400, "code", "BAD_REQUEST", "message", ex.getMessage());
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public Map<String,Object> handleConflict(DataIntegrityViolationException ex){
        return Map.of("status", 409, "code", "CONFLICT", "message", ex.getMostSpecificCause().getMessage());
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Map<String,Object> handleAll(Exception ex){
        return Map.of("status", 500, "code", "INTERNAL_ERROR", "message", ex.getMessage());
    }
}
