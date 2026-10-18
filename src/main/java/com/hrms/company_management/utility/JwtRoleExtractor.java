package com.hrms.company_management.utility;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashSet;
import java.util.Set;

/**
 * Reads Keycloak realm roles (realm_access.roles) from a bearer token.
 * The signature is NOT checked here: Kong validates the token before the request reaches this service.
 */
@Component
@Log4j2
public class JwtRoleExtractor {

    private static final String BEARER_PREFIX = "Bearer ";

    private final ObjectMapper objectMapper = new ObjectMapper();

    public Set<String> extractRealmRoles(String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.regionMatches(true, 0, BEARER_PREFIX, 0, BEARER_PREFIX.length())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Missing bearer token");
        }
        String token = authorizationHeader.substring(BEARER_PREFIX.length()).trim();
        String[] parts = token.split("\\.");
        if (parts.length < 2) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Malformed token");
        }

        try {
            byte[] payload = Base64.getUrlDecoder().decode(parts[1]);
            JsonNode roles = objectMapper.readTree(new String(payload, StandardCharsets.UTF_8))
                    .path("realm_access").path("roles");

            Set<String> result = new HashSet<>();
            roles.forEach(role -> result.add(role.asText()));
            return result;
        } catch (Exception ex) {
            log.error("Unable to read roles from token", ex);
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Malformed token");
        }
    }
}
