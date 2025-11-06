//package com.example.springssodemo.service;
//
//import com.example.springssodemo.dto.SsoConfigDto;
//import com.example.springssodemo.model.SsoConfig;
//import org.junit.jupiter.api.Test;
//import org.springframework.boot.test.context.SpringBootTest;
//
//import static org.junit.jupiter.api.Assertions.*;
//
///**
// * Basic JUnit 5 test to validate SsoConfig structure.
// */
//@SpringBootTest
//class SsoConfigServiceTests {
//
//    @Test
//    void testSsoConfigDtoFields() {
//        SsoConfigDto dto = new SsoConfigDto();
//        dto.setProviderId("oidc_miniorange");
//        dto.setType("OIDC");
//        dto.setDisplayName("MiniOrange OIDC");
//
//        assertEquals("oidc_miniorange", dto.getProviderId());
//        assertEquals("OIDC", dto.getType());
//        assertEquals("MiniOrange OIDC", dto.getDisplayName());
//    }
//
//    @Test
//    void testSsoConfigEntityDefaults() {
//        SsoConfig sso = new SsoConfig();
//        sso.setProviderId("saml_okta");
//        sso.setType("SAML");
//        sso.setDisplayName("Okta SAML");
//        sso.setHasSecret(false);
//
//        assertEquals("saml_okta", sso.getProviderId());
//        assertEquals("SAML", sso.getType());
//        assertEquals("Okta SAML", sso.getDisplayName());
//        assertFalse(sso.isHasSecret());
//    }
//}
