package com.hrms.company_management.utility;

import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import com.fasterxml.jackson.databind.JsonNode;
import com.hrms.company_management.dto.NoticeCaller;

import lombok.extern.log4j.Log4j2;

/**
 * Checks who is calling a notice endpoint and for which tenant.
 * The token signature is validated by Kong; this class only reads the claims.
 */
@Component
@Log4j2
public class NoticeAccessGuard {

    private final JwtRoleExtractor jwtRoleExtractor;
    private final TenantRegistry tenantRegistry;
    private final Set<String> adminRoles;
    private final String tenantClaim;

    public NoticeAccessGuard(JwtRoleExtractor jwtRoleExtractor,
                             TenantRegistry tenantRegistry,
                             @Value("${notices.security.admin-roles:admin}") String adminRoles,
                             @Value("${notices.security.tenant-claim:}") String tenantClaim) {
        this.jwtRoleExtractor = jwtRoleExtractor;
        this.tenantRegistry = tenantRegistry;
        this.adminRoles = Arrays.stream(adminRoles.split(","))
                .map(String::trim)
                .filter(role -> !role.isEmpty())
                .collect(Collectors.toUnmodifiableSet());
        this.tenantClaim = tenantClaim == null ? "" : tenantClaim.trim();
    }

    /** Any authenticated user of a known tenant. */
    public NoticeCaller requireEmployee(String authorizationHeader) {
        JsonNode claims = jwtRoleExtractor.extractClaims(authorizationHeader);
        String userId = claims.path("sub").asText("");
        if (userId.isBlank()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Token has no subject");
        }
        String tenantId = requireTenant(claims);
        return new NoticeCaller(userId, tenantId, Collections.unmodifiableSet(jwtRoleExtractor.realmRoles(claims)));
    }

    /** An authenticated user holding one of the configured notice-admin roles. */
    public NoticeCaller requireAdmin(String authorizationHeader) {
        NoticeCaller caller = requireEmployee(authorizationHeader);
        if (Collections.disjoint(caller.roles(), adminRoles)) {
            log.warn("User {} of tenant {} denied notice admin access", caller.userId(), caller.tenantId());
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Notice administration requires one of roles " + adminRoles);
        }
        return caller;
    }

    private String requireTenant(JsonNode claims) {
        String tenantId = TenantContext.getCurrentTenant();
        if (tenantId == null || tenantId.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "X-Tenant-Id header is required");
        }
        if (!tenantRegistry.isKnownTenant(tenantId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Unknown tenant");
        }
        if (!tenantClaim.isEmpty() && !tokenBelongsToTenant(claims.path(tenantClaim), tenantId)) {
            log.warn("Token tenant claim '{}' does not match X-Tenant-Id {}", tenantClaim, tenantId);
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Token does not belong to this tenant");
        }
        return tenantId;
    }

    private static boolean tokenBelongsToTenant(JsonNode claim, String tenantId) {
        if (claim.isArray()) {
            for (JsonNode value : claim) {
                if (tenantId.equals(value.asText())) {
                    return true;
                }
            }
            return false;
        }
        return claim.isValueNode() && tenantId.equals(claim.asText());
    }
}
