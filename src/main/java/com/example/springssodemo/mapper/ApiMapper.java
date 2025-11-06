package com.example.springssodemo.mapper;

import com.example.springssodemo.dto.SsoConfigDto;
import com.example.springssodemo.dto.UserDto;
import com.example.springssodemo.model.Role; // <-- IMPORT
import com.example.springssodemo.model.SsoConfig;
import com.example.springssodemo.model.User;
import com.example.springssodemo.model.UserRole; // <-- IMPORT
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Map;
import java.util.Objects; // <-- IMPORT
import java.util.stream.Collectors;

public class ApiMapper {

    private static final ObjectMapper mapper = new ObjectMapper();

    public static UserDto toDto(User u) {
        if (u == null) return null;
        UserDto d = new UserDto();
        d.setId(u.getId());
        d.setUsername(u.getUsername());
        d.setEmail(u.getEmail());
        d.setFirstName(u.getFirstName());
        d.setLastName(u.getLastName());
        d.setProvider(u.getProvider());

        // ✅ FIXED MAPPING
        if (u.getUserRoles() != null) { // Use getUserRoles()
            List<String> roles = u.getUserRoles().stream()
                    .filter(Objects::nonNull)
                    .map(UserRole::getRole) // Get Role object from UserRole
                    .filter(Objects::nonNull)
                    .map(Role::getName)     // Get String name from Role
                    .collect(Collectors.toList());
            d.setRoles(roles);
        }
        return d;
    }

    public static SsoConfigDto toDto(SsoConfig s) {
        if (s == null) return null;
        SsoConfigDto d = new SsoConfigDto();
        d.setId(s.getId());
        d.setProviderId(s.getProviderId());
        d.setType(s.getType());
        d.setDisplayName(s.getDisplayName());
        d.setHasSecret(s.isHasSecret());
        d.setEnabled(s.isEnabled());

        if (s.getSettings() != null) {
            Map<String, Object> settingsMap = mapper.convertValue(
                    s.getSettings(), new TypeReference<Map<String, Object>>() {}
            );
            settingsMap.remove("clientSecret");
            JsonNode node = mapper.valueToTree(settingsMap);
            d.setSettings(node);
        } else {
            d.setSettings(null);
        }
        return d;
    }
}