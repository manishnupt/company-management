package com.hrms.company_management.notice;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.hrms.company_management.service.NoticeLifecycleScheduler;
import com.hrms.company_management.service.NoticeService;
import com.hrms.company_management.utility.TenantContext;
import com.hrms.company_management.utility.TenantRegistry;

class NoticeLifecycleSchedulerTest {

    @Test
    void runsForEveryTenantWithItsContextAndSurvivesFailures() {
        NoticeService service = mock(NoticeService.class);
        TenantRegistry registry = mock(TenantRegistry.class);
        when(registry.isRoutingEnabled()).thenReturn(true);
        when(registry.tenantIds()).thenReturn(new LinkedHashSet<>(List.of("broken", "tomato")));

        List<String> seenTenants = new ArrayList<>();
        when(service.applyScheduledTransitions()).thenAnswer(inv -> {
            String tenant = TenantContext.getCurrentTenant();
            seenTenants.add(tenant);
            if ("broken".equals(tenant)) {
                throw new IllegalStateException("column does not exist");
            }
            return new NoticeService.TransitionResult(1, 0);
        });

        new NoticeLifecycleScheduler(service, registry).applyTransitions();

        assertThat(seenTenants).containsExactly("broken", "tomato");
        assertThat(TenantContext.getCurrentTenant()).isNull();
        verify(service, times(2)).applyScheduledTransitions();
    }

    @Test
    void runsOnceWithoutRouting() {
        NoticeService service = mock(NoticeService.class);
        TenantRegistry registry = mock(TenantRegistry.class);
        when(registry.isRoutingEnabled()).thenReturn(false);
        when(service.applyScheduledTransitions()).thenReturn(new NoticeService.TransitionResult(0, 0));

        new NoticeLifecycleScheduler(service, registry).applyTransitions();
        verify(service, times(1)).applyScheduledTransitions();
    }
}
