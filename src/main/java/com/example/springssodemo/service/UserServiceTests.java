//package com.example.springssodemo.service;
//
//import com.example.springssodemo.dto.UserDto;
//import com.example.springssodemo.model.User;
//import org.junit.jupiter.api.Test;
//import org.springframework.boot.test.context.SpringBootTest;
//
//import static org.junit.jupiter.api.Assertions.*;
//
///**
// * Basic JUnit 5 test for User and UserDto data models.
// */
//@SpringBootTest
//class UserServiceTests {
//
//    @Test
//    void testUserDtoFields() {
//        UserDto dto = new UserDto();
//        dto.setUsername("shlok");
//        dto.setEmail("shlok@example.com");
//        dto.setPassword("password123");
//
//        assertEquals("shlok", dto.getUsername());
//        assertEquals("shlok@example.com", dto.getEmail());
//        assertEquals("password123", dto.getPassword());
//    }
//
//    @Test
//    void testUserEntityFields() {
//        User user = new User();
//        user.setUsername("admin");
//        user.setEmail("admin@example.com");
//        user.setPasswordHash("hashed");
//
//        assertEquals("admin", user.getUsername());
//        assertEquals("admin@example.com", user.getEmail());
//        assertEquals("hashed", user.getPasswordHash());
//    }
//}
