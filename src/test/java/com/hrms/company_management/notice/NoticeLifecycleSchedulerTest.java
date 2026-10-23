package com.hrms.company_management.notice;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.scheduling.annotation.Scheduled;

import com.hrms.company_management.config.MultiTenantConfiguration;
import com.hrms.company_management.service.NoticeLifecycleScheduler;
import com.hrms.company_management.service.NoticeService;
import com.hrms.company_management.utility.TenantContext;

class NoticeLifecycleSchedulerTest {

    @Test
    void runsForEveryConfiguredTenantWithItsContextAndSurvivesFailures() {
        NoticeService service = mock(NoticeService.class);
        MultiTenantConfiguration tenants = mock(MultiTenantConfiguration.class);
        when(tenants.getTenantIds()).thenReturn(new LinkedHashSet<>(List.of("broken", "tomato")));

        List<String> seenTenants = new ArrayList<>();
        when(service.applyScheduledTransitions()).thenAnswer(inv -> {
            String tenant = TenantContext.getCurrentTenant();
            seenTenants.add(tenant);
            if ("broken".equals(tenant)) {
                throw new IllegalStateException("column does not exist");
            }
            return new NoticeService.TransitionResult(1, 0);
        });

        new NoticeLifecycleScheduler(service, tenants).applyTransitions();

        assertThat(seenTenants).containsExactly("broken", "tomato");
        assertThat(TenantContext.getCurrentTenant()).isNull();
        verify(service, times(2)).applyScheduledTransitions();
    }

    @Test
    void noTenantsMeansNoWork() {
        NoticeService service = mock(NoticeService.class);
        MultiTenantConfiguration tenants = mock(MultiTenantConfiguration.class);
        when(tenants.getTenantIds()).thenReturn(java.util.Set.of());

        new NoticeLifecycleScheduler(service, tenants).applyTransitions();
        verify(service, times(0)).applyScheduledTransitions();
    }

    @Test
    void scheduledDailyAtOneAmIst() throws Exception {
        Method method = NoticeLifecycleScheduler.class.getMethod("applyTransitions");
        Scheduled scheduled = method.getAnnotation(Scheduled.class);
        assertThat(scheduled.cron()).isEqualTo("${notices.scheduler.cron:0 0 1 * * *}");
        assertThat(scheduled.zone()).isEqualTo("${notices.scheduler.zone:Asia/Kolkata}");
    }
}
