package com.hrms.company_management.notice;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import com.hrms.company_management.entity.Notice;
import com.hrms.company_management.entity.NoticeStatus;
import com.hrms.company_management.repository.NoticeRepo;
import com.hrms.company_management.utility.NoticeLifecycle;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
class NoticeRepoTest {

    private static final Instant NOW = Instant.now().truncatedTo(ChronoUnit.SECONDS);

    @Autowired
    private NoticeRepo noticeRepo;

    private Notice save(String title, NoticeStatus status, Instant publishAt, Instant expiresAt) {
        Notice notice = new Notice();
        notice.setTitle(title);
        notice.setDescription("d");
        notice.setNoticeType("GENERAL");
        notice.setStatus(status);
        notice.setPublishAt(publishAt);
        notice.setExpiresAt(expiresAt);
        return noticeRepo.saveAndFlush(notice);
    }

    @Test
    void visibleQueryReturnsOnlyLiveNoticesNewestFirst() {
        save("legacy", null, null, null);
        save("old", NoticeStatus.PUBLISHED, NOW.minusSeconds(7200), null);
        save("new", NoticeStatus.PUBLISHED, NOW.minusSeconds(60), NOW.plusSeconds(3600));
        save("due", NoticeStatus.SCHEDULED, NOW.minusSeconds(1), null);
        save("future", NoticeStatus.SCHEDULED, NOW.plusSeconds(60), null);
        save("expired", NoticeStatus.PUBLISHED, NOW.minusSeconds(7200), NOW);
        save("draft", NoticeStatus.DRAFT, null, null);
        save("archived", NoticeStatus.ARCHIVED, NOW.minusSeconds(60), null);

        assertThat(noticeRepo.findVisible(NoticeLifecycle.LIVE_STATUSES, NOW))
                .extracting(Notice::getTitle)
                .containsExactly("due", "new", "old", "legacy");
    }

    @Test
    void findVisibleByIdHidesDrafts() {
        Notice draft = save("draft", NoticeStatus.DRAFT, null, null);
        Notice live = save("live", NoticeStatus.PUBLISHED, NOW.minusSeconds(1), null);

        assertThat(noticeRepo.findVisibleById(draft.getId(), NoticeLifecycle.LIVE_STATUSES, NOW)).isEmpty();
        assertThat(noticeRepo.findVisibleById(live.getId(), NoticeLifecycle.LIVE_STATUSES, NOW)).isPresent();
    }

    @Test
    void bulkTransitionsPublishThenExpire() {
        Notice due = save("due", NoticeStatus.SCHEDULED, NOW.minusSeconds(10), null);
        Notice dueAndExpired = save("dueAndExpired", NoticeStatus.SCHEDULED, NOW.minusSeconds(10), NOW.minusSeconds(5));
        Notice future = save("future", NoticeStatus.SCHEDULED, NOW.plusSeconds(10), null);
        Notice expired = save("expired", NoticeStatus.PUBLISHED, NOW.minusSeconds(100), NOW.minusSeconds(1));
        Notice draft = save("draft", NoticeStatus.DRAFT, NOW.minusSeconds(100), NOW.minusSeconds(1));
        LocalDate today = LocalDate.now();

        assertThat(noticeRepo.publishDue(NoticeStatus.SCHEDULED, NoticeStatus.PUBLISHED, NOW, today)).isEqualTo(2);
        assertThat(noticeRepo.expireDue(NoticeLifecycle.LIVE_STATUSES, NoticeStatus.EXPIRED, NOW, today)).isEqualTo(2);

        assertThat(noticeRepo.findById(due.getId()).orElseThrow().getStatus()).isEqualTo(NoticeStatus.PUBLISHED);
        assertThat(noticeRepo.findById(dueAndExpired.getId()).orElseThrow().getStatus()).isEqualTo(NoticeStatus.EXPIRED);
        assertThat(noticeRepo.findById(future.getId()).orElseThrow().getStatus()).isEqualTo(NoticeStatus.SCHEDULED);
        assertThat(noticeRepo.findById(expired.getId()).orElseThrow().getStatus()).isEqualTo(NoticeStatus.EXPIRED);
        assertThat(noticeRepo.findById(draft.getId()).orElseThrow().getStatus()).isEqualTo(NoticeStatus.DRAFT);
        assertThat(noticeRepo.findById(due.getId()).orElseThrow().getUpdatedAt()).isEqualTo(today);
    }
}
