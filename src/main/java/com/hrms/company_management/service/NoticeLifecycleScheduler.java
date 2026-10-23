package com.hrms.company_management.service;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.hrms.company_management.utility.TenantContext;
import com.hrms.company_management.utility.TenantRegistry;

import lombok.extern.log4j.Log4j2;

/**
 * Periodically stores SCHEDULED -> PUBLISHED and -> EXPIRED transitions in every tenant database.
 * Optional: visibility is computed from publishAt/expiresAt at read time, so this only keeps stored
 * statuses tidy. The updates are idempotent, so running it on several instances is safe.
 */
@Component
@ConditionalOnProperty(name = "notices.scheduler.enabled", havingValue = "true", matchIfMissing = true)
@Log4j2
public class NoticeLifecycleScheduler {

    private final NoticeService noticeService;
    private final TenantRegistry tenantRegistry;

    public NoticeLifecycleScheduler(NoticeService noticeService, TenantRegistry tenantRegistry) {
        this.noticeService = noticeService;
        this.tenantRegistry = tenantRegistry;
    }

    @Scheduled(fixedDelayString = "${notices.scheduler.fixed-delay-ms:60000}",
            initialDelayString = "${notices.scheduler.initial-delay-ms:30000}")
    public void applyTransitions() {
        if (!tenantRegistry.isRoutingEnabled()) {
            applyForTenant(null);
            return;
        }
        for (String tenantId : tenantRegistry.tenantIds()) {
            applyForTenant(tenantId);
        }
    }

    /** One tenant failing (e.g. columns not added yet) must not stop the others. */
    void applyForTenant(String tenantId) {
        String previous = TenantContext.getCurrentTenant();
        try {
            // Must be set before the transaction opens, because the routing DataSource picks the DB on connect.
            TenantContext.setCurrentTenant(tenantId);
            NoticeService.TransitionResult result = noticeService.applyScheduledTransitions();
            if (result.published() > 0 || result.expired() > 0) {
                log.info("Tenant {}: {} notices published, {} expired", tenantId, result.published(), result.expired());
            }
        } catch (Exception ex) {
            log.error("Notice lifecycle update failed for tenant {}", tenantId, ex);
        } finally {
            TenantContext.setCurrentTenant(previous);
        }
    }
}
