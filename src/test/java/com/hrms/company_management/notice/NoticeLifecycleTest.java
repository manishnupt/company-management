package com.hrms.company_management.notice;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;

import org.junit.jupiter.api.Test;

import com.hrms.company_management.entity.Notice;
import com.hrms.company_management.entity.NoticeStatus;
import com.hrms.company_management.utility.NoticeLifecycle;

class NoticeLifecycleTest {

    private static final Instant NOW = Instant.parse("2026-10-24T10:00:00Z");

    private static Notice notice(NoticeStatus status, Instant publishAt, Instant expiresAt) {
        Notice notice = new Notice();
        notice.setStatus(status);
        notice.setPublishAt(publishAt);
        notice.setExpiresAt(expiresAt);
        return notice;
    }

    @Test
    void statusOnPublishSchedulesOnlyFutureNotices() {
        assertThat(NoticeLifecycle.statusOnPublish(null, NOW)).isEqualTo(NoticeStatus.PUBLISHED);
        assertThat(NoticeLifecycle.statusOnPublish(NOW, NOW)).isEqualTo(NoticeStatus.PUBLISHED);
        assertThat(NoticeLifecycle.statusOnPublish(NOW.plusSeconds(1), NOW)).isEqualTo(NoticeStatus.SCHEDULED);
    }

    @Test
    void legacyRowWithNullsIsPublished() {
        Notice legacy = notice(null, null, null);
        assertThat(NoticeLifecycle.effectiveStatus(legacy, NOW)).isEqualTo(NoticeStatus.PUBLISHED);
        assertThat(NoticeLifecycle.isVisible(legacy, NOW)).isTrue();
    }

    @Test
    void scheduledBecomesPublishedOncePublishAtPasses() {
        Notice scheduled = notice(NoticeStatus.SCHEDULED, NOW.plusSeconds(60), null);
        assertThat(NoticeLifecycle.effectiveStatus(scheduled, NOW)).isEqualTo(NoticeStatus.SCHEDULED);
        assertThat(NoticeLifecycle.effectiveStatus(scheduled, NOW.plusSeconds(60))).isEqualTo(NoticeStatus.PUBLISHED);
    }

    @Test
    void liveNoticeExpiresAtExpiresAt() {
        Notice published = notice(NoticeStatus.PUBLISHED, NOW.minusSeconds(60), NOW);
        assertThat(NoticeLifecycle.effectiveStatus(published, NOW)).isEqualTo(NoticeStatus.EXPIRED);
        assertThat(NoticeLifecycle.effectiveStatus(published, NOW.minusSeconds(1))).isEqualTo(NoticeStatus.PUBLISHED);
    }

    @Test
    void draftAndArchivedIgnoreDates() {
        assertThat(NoticeLifecycle.effectiveStatus(notice(NoticeStatus.DRAFT, NOW.minusSeconds(10), NOW.minusSeconds(5)), NOW))
                .isEqualTo(NoticeStatus.DRAFT);
        assertThat(NoticeLifecycle.effectiveStatus(notice(NoticeStatus.ARCHIVED, NOW.minusSeconds(10), null), NOW))
                .isEqualTo(NoticeStatus.ARCHIVED);
    }

    @Test
    void onlyDraftAndScheduledAreEditable() {
        assertThat(NoticeLifecycle.isEditable(notice(NoticeStatus.DRAFT, null, null), NOW)).isTrue();
        assertThat(NoticeLifecycle.isEditable(notice(NoticeStatus.SCHEDULED, NOW.plusSeconds(5), null), NOW)).isTrue();
        // Stored SCHEDULED but already due: employees can see it, so it is no longer editable.
        assertThat(NoticeLifecycle.isEditable(notice(NoticeStatus.SCHEDULED, NOW.minusSeconds(5), null), NOW)).isFalse();
        assertThat(NoticeLifecycle.isEditable(notice(NoticeStatus.PUBLISHED, NOW.minusSeconds(5), null), NOW)).isFalse();
    }
}
