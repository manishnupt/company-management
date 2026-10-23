package com.hrms.company_management.service;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.hrms.company_management.config.MultiTenantConfiguration;
import com.hrms.company_management.utility.TenantContext;

import lombok.extern.log4j.Log4j2;

/**
 * Once a day, stores SCHEDULED -> PUBLISHED and -> EXPIRED transitions in every tenant database.
 * The API already computes the status from publishAt/expiresAt on every read, so this only keeps the
 * stored status column tidy; it may lag by up to a day. The updates are idempotent, so a missed run is
 * caught up by the next one and running on several instances is safe.
 */
@Component
@ConditionalOnProperty(name = "notices.scheduler.enabled", havingValue = "true", matchIfMissing = true)
@Log4j2
public class NoticeLifecycleScheduler {

    private final NoticeService noticeService;
    private final MultiTenantConfiguration multiTenantConfiguration;

    public NoticeLifecycleScheduler(NoticeService noticeService, MultiTenantConfiguration multiTenantConfiguration) {
        this.noticeService = noticeService;
        this.multiTenantConfiguration = multiTenantConfiguration;
    }

    @Scheduled(cron = "${notices.scheduler.cron:0 0 1 * * *}", zone = "${notices.scheduler.zone:Asia/Kolkata}")
    public void applyTransitions() {
        for (String tenantId : multiTenantConfiguration.getTenantIds()) {
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
            log.info("Tenant {}: {} notices published, {} expired", tenantId, result.published(), result.expired());
        } catch (Exception ex) {
            log.error("Notice lifecycle update failed for tenant {}", tenantId, ex);
        } finally {
            TenantContext.setCurrentTenant(previous);
        }
    }
}
