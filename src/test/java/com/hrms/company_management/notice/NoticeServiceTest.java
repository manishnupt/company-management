package com.hrms.company_management.notice;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import com.hrms.company_management.dto.NoticeRequest;
import com.hrms.company_management.dto.NoticeResponse;
import com.hrms.company_management.entity.Notice;
import com.hrms.company_management.entity.NoticeStatus;
import com.hrms.company_management.repository.NoticeRepo;
import com.hrms.company_management.service.NoticeService;
import com.hrms.company_management.utility.NoticeMapper;

@ExtendWith(MockitoExtension.class)
class NoticeServiceTest {

    private static final Instant NOW = Instant.parse("2026-10-24T10:00:00Z");

    @Mock
    private NoticeRepo noticeRepo;

    private NoticeService service;

    @BeforeEach
    void setUp() {
        service = new NoticeService(noticeRepo, new NoticeMapper(new ModelMapper()),
                Clock.fixed(NOW, ZoneOffset.UTC));
    }

    private static NoticeRequest request() {
        NoticeRequest request = new NoticeRequest();
        request.setTitle("  Office closed ");
        request.setDescription("Diwali break");
        request.setNoticeType("GENERAL");
        return request;
    }

    private static Notice stored(Long id, NoticeStatus status, Instant publishAt, Instant expiresAt) {
        Notice notice = new Notice();
        notice.setId(id);
        notice.setTitle("t");
        notice.setDescription("d");
        notice.setNoticeType("GENERAL");
        notice.setStatus(status);
        notice.setPublishAt(publishAt);
        notice.setExpiresAt(expiresAt);
        return notice;
    }

    private void saveReturnsArgument() {
        when(noticeRepo.save(any(Notice.class))).thenAnswer(inv -> {
            Notice n = inv.getArgument(0);
            if (n.getId() == null) {
                n.setId(1L);
            }
            return n;
        });
    }

    private static void assertStatus(Runnable call, HttpStatus expected) {
        assertThatThrownBy(call::run)
                .isInstanceOfSatisfying(ResponseStatusException.class,
                        ex -> assertThat(ex.getStatusCode()).isEqualTo(expected));
    }

    @Test
    void createWithoutPublishAtPublishesNow() {
        saveReturnsArgument();
        NoticeResponse response = service.createNotice(request());

        assertThat(response.getStatus()).isEqualTo(NoticeStatus.PUBLISHED);
        assertThat(response.getPublishAt()).isEqualTo(NOW);
        assertThat(response.getTitle()).isEqualTo("Office closed");
    }

    @Test
    void createWithFuturePublishAtSchedules() {
        saveReturnsArgument();
        NoticeRequest request = request();
        request.setPublishAt(NOW.plusSeconds(3600));

        assertThat(service.createNotice(request).getStatus()).isEqualTo(NoticeStatus.SCHEDULED);
    }

    @Test
    void createAsDraftKeepsDraft() {
        saveReturnsArgument();
        NoticeRequest request = request();
        request.setSaveAsDraft(true);

        assertThat(service.createNotice(request).getStatus()).isEqualTo(NoticeStatus.DRAFT);
    }

    @Test
    void createRejectsExpiryBeforePublishOrInThePast() {
        NoticeRequest beforePublish = request();
        beforePublish.setPublishAt(NOW.plusSeconds(100));
        beforePublish.setExpiresAt(NOW.plusSeconds(50));
        assertStatus(() -> service.createNotice(beforePublish), HttpStatus.BAD_REQUEST);

        NoticeRequest past = request();
        past.setExpiresAt(NOW.minusSeconds(1));
        assertStatus(() -> service.createNotice(past), HttpStatus.BAD_REQUEST);
        verify(noticeRepo, never()).save(any());
    }

    @Test
    void publishDraftWithFutureDateSchedulesIt() {
        Notice draft = stored(5L, NoticeStatus.DRAFT, NOW.plusSeconds(600), null);
        when(noticeRepo.findById(5L)).thenReturn(Optional.of(draft));
        saveReturnsArgument();

        assertThat(service.publishNotice(5L).getStatus()).isEqualTo(NoticeStatus.SCHEDULED);
    }

    @Test
    void publishScheduledPublishesImmediately() {
        Notice scheduled = stored(5L, NoticeStatus.SCHEDULED, NOW.plusSeconds(600), null);
        when(noticeRepo.findById(5L)).thenReturn(Optional.of(scheduled));
        saveReturnsArgument();

        NoticeResponse response = service.publishNotice(5L);
        assertThat(response.getStatus()).isEqualTo(NoticeStatus.PUBLISHED);
        assertThat(response.getPublishAt()).isEqualTo(NOW);
    }

    @Test
    void publishStaleDraftWhoseExpiryPassedIsRejected() {
        Notice draft = stored(5L, NoticeStatus.DRAFT, null, NOW.minusSeconds(1));
        when(noticeRepo.findById(5L)).thenReturn(Optional.of(draft));

        assertStatus(() -> service.publishNotice(5L), HttpStatus.BAD_REQUEST);
    }

    @Test
    void publishedNoticeCannotBeEditedPublishedOrDeleted() {
        Notice published = stored(5L, NoticeStatus.PUBLISHED, NOW.minusSeconds(60), null);
        when(noticeRepo.findById(5L)).thenReturn(Optional.of(published));

        assertStatus(() -> service.updateNotice(5L, request()), HttpStatus.CONFLICT);
        assertStatus(() -> service.publishNotice(5L), HttpStatus.CONFLICT);
        assertStatus(() -> service.deleteNotice(5L), HttpStatus.CONFLICT);
    }

    @Test
    void editingScheduledNoticeToPastDatePublishesIt() {
        Notice scheduled = stored(5L, NoticeStatus.SCHEDULED, NOW.plusSeconds(600), null);
        when(noticeRepo.findById(5L)).thenReturn(Optional.of(scheduled));
        saveReturnsArgument();

        NoticeResponse response = service.updateNotice(5L, request());
        assertThat(response.getStatus()).isEqualTo(NoticeStatus.PUBLISHED);
    }

    @Test
    void archiveTwiceIsRejected() {
        Notice notice = stored(5L, NoticeStatus.PUBLISHED, NOW.minusSeconds(60), null);
        when(noticeRepo.findById(5L)).thenReturn(Optional.of(notice));
        saveReturnsArgument();

        assertThat(service.archiveNotice(5L).getStatus()).isEqualTo(NoticeStatus.ARCHIVED);
        assertStatus(() -> service.archiveNotice(5L), HttpStatus.CONFLICT);
    }

    @Test
    void deleteDraftRemovesNotice() {
        Notice draft = stored(5L, NoticeStatus.DRAFT, null, null);
        when(noticeRepo.findById(5L)).thenReturn(Optional.of(draft));

        service.deleteNotice(5L);
        verify(noticeRepo).delete(draft);
    }

    @Test
    void unknownNoticeIs404() {
        when(noticeRepo.findById(99L)).thenReturn(Optional.empty());
        assertStatus(() -> service.getNotice(99L), HttpStatus.NOT_FOUND);
    }

    @Test
    void adminListFiltersByEffectiveStatus() {
        when(noticeRepo.findAllByOrderByIdDesc()).thenReturn(List.of(
                stored(1L, NoticeStatus.DRAFT, null, null),
                // Stored SCHEDULED but due: counts as PUBLISHED.
                stored(2L, NoticeStatus.SCHEDULED, NOW.minusSeconds(1), null),
                stored(3L, null, null, null)));

        assertThat(service.getAllNotices(NoticeStatus.PUBLISHED)).extracting(NoticeResponse::getId).containsExactly(2L, 3L);
        assertThat(service.getAllNotices(null)).hasSize(3);
    }

    @Test
    void employeeListReturnsVisibleNotices() {
        when(noticeRepo.findVisible(anyCollection(), eq(NOW)))
                .thenReturn(List.of(stored(1L, NoticeStatus.PUBLISHED, NOW.minusSeconds(60), null)));

        assertThat(service.getVisibleNotices()).extracting(NoticeResponse::getId).containsExactly(1L);
    }

    @Test
    void hiddenNoticeIs404ForEmployees() {
        when(noticeRepo.findVisibleById(eq(1L), anyCollection(), eq(NOW))).thenReturn(Optional.empty());
        assertStatus(() -> service.getVisibleNotice(1L), HttpStatus.NOT_FOUND);
    }
}
