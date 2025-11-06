package com.example.springssodemo.mapper;

import com.example.springssodemo.dto.SsoConfigDto;
import com.example.springssodemo.dto.UserDto;
import com.example.springssodemo.model.SsoConfig;
import com.example.springssodemo.model.User;
import com.example.springssodemo.model.UserRole;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.*;
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
        if (u.getRoles() != null) {
            List<String> roles = u.getRoles().stream()
                    .filter(Objects::nonNull)
                    .map(UserRole::getRole)
                    .filter(Objects::nonNull)
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

        // ✅ Convert JsonNode -> Map, sanitize, then back to JsonNode
        if (s.getSettings() != null) {
            Map<String, Object> settingsMap = mapper.convertValue(
                    s.getSettings(), new TypeReference<Map<String, Object>>() {}
            );
            settingsMap.remove("clientSecret"); // sanitize sensitive field

            JsonNode node = mapper.valueToTree(settingsMap); // ✅ Convert back to JsonNode
            d.setSettings(node);
        } else {
            d.setSettings(null);
        }

        return d;
    }
}
