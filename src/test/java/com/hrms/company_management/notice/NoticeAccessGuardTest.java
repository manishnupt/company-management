package com.hrms.company_management.notice;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import com.hrms.company_management.dto.NoticeCaller;
import com.hrms.company_management.utility.JwtRoleExtractor;
import com.hrms.company_management.utility.NoticeAccessGuard;
import com.hrms.company_management.utility.TenantContext;
import com.hrms.company_management.utility.TenantRegistry;

class NoticeAccessGuardTest {

    private final TenantRegistry tenantRegistry = mock(TenantRegistry.class);
    private final NoticeAccessGuard guard =
            new NoticeAccessGuard(new JwtRoleExtractor(), tenantRegistry, "hr-admin, admin", "");

    @BeforeEach
    void setUp() {
        TenantContext.setCurrentTenant("tomato");
        when(tenantRegistry.isKnownTenant("tomato")).thenReturn(true);
    }

    @AfterEach
    void tearDown() {
        TenantContext.setCurrentTenant(null);
    }

    private static void assertStatus(Runnable call, HttpStatus expected) {
        assertThatThrownBy(call::run)
                .isInstanceOfSatisfying(ResponseStatusException.class,
                        ex -> assertThat(ex.getStatusCode()).isEqualTo(expected));
    }

    @Test
    void employeeGetsSubjectTenantAndRoles() {
        NoticeCaller caller = guard.requireEmployee(TestTokens.bearer("emp-1", "employee"));
        assertThat(caller.userId()).isEqualTo("emp-1");
        assertThat(caller.tenantId()).isEqualTo("tomato");
        assertThat(caller.roles()).containsExactly("employee");
    }

    @Test
    void adminRoleIsRequiredForAdmin() {
        assertThat(guard.requireAdmin(TestTokens.bearer("a", "hr-admin")).userId()).isEqualTo("a");
        assertStatus(() -> guard.requireAdmin(TestTokens.bearer("e", "employee")), HttpStatus.FORBIDDEN);
    }

    @Test
    void missingOrBrokenTokenIs401() {
        assertStatus(() -> guard.requireEmployee(null), HttpStatus.UNAUTHORIZED);
        assertStatus(() -> guard.requireEmployee("Bearer garbage"), HttpStatus.UNAUTHORIZED);
        assertStatus(() -> guard.requireEmployee(TestTokens.bearerWithPayload("{\"realm_access\":{}}")), HttpStatus.UNAUTHORIZED);
    }

    @Test
    void missingTenantIs400AndUnknownTenantIs403() {
        TenantContext.setCurrentTenant("");
        assertStatus(() -> guard.requireEmployee(TestTokens.bearer("e")), HttpStatus.BAD_REQUEST);

        TenantContext.setCurrentTenant("other");
        assertStatus(() -> guard.requireEmployee(TestTokens.bearer("e")), HttpStatus.FORBIDDEN);
    }

    @Test
    void tenantClaimMustMatchWhenConfigured() {
        NoticeAccessGuard strict = new NoticeAccessGuard(new JwtRoleExtractor(), tenantRegistry, "admin", "tenant");

        assertThat(strict.requireEmployee(TestTokens.bearerWithPayload("{\"sub\":\"e\",\"tenant\":\"tomato\"}")).tenantId())
                .isEqualTo("tomato");
        assertThat(strict.requireEmployee(TestTokens.bearerWithPayload("{\"sub\":\"e\",\"tenant\":[\"x\",\"tomato\"]}")).tenantId())
                .isEqualTo("tomato");
        assertStatus(() -> strict.requireEmployee(TestTokens.bearerWithPayload("{\"sub\":\"e\",\"tenant\":\"potato\"}")),
                HttpStatus.FORBIDDEN);
        assertStatus(() -> strict.requireEmployee(TestTokens.bearerWithPayload("{\"sub\":\"e\"}")), HttpStatus.FORBIDDEN);
    }
}
