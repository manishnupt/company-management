package com.hrms.company_management.service;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.hrms.company_management.dto.NoticeCaller;
import com.hrms.company_management.dto.NoticeRequest;
import com.hrms.company_management.dto.NoticeResponse;
import com.hrms.company_management.entity.Notice;
import com.hrms.company_management.entity.NoticeStatus;
import com.hrms.company_management.repository.NoticeRepo;
import com.hrms.company_management.utility.NoticeLifecycle;
import com.hrms.company_management.utility.NoticeMapper;

import lombok.extern.log4j.Log4j2;

/**
 * Notice lifecycle (draft, schedule, publish, expire, archive) and employee visibility.
 * All queries run against the current tenant's database, chosen by TenantContext.
 */
@Service
@Log4j2
public class NoticeService {

    public record TransitionResult(int published, int expired) {
    }

    private final NoticeRepo noticeRepo;
    private final NoticeMapper noticeMapper;
    private final Clock clock;

    public NoticeService(NoticeRepo noticeRepo,
                         NoticeMapper noticeMapper,
                         Clock clock) {
        this.noticeRepo = noticeRepo;
        this.noticeMapper = noticeMapper;
        this.clock = clock;
    }

    // ---------- Admin ----------

    @Transactional
    public NoticeResponse createNotice(NoticeRequest request, NoticeCaller caller) {
        Instant now = clock.instant();
        validateDates(request.getPublishAt(), request.getExpiresAt(), now);

        Notice notice = new Notice();
        noticeMapper.applyRequest(request, notice);
        if (request.isSaveAsDraft()) {
            notice.setStatus(NoticeStatus.DRAFT);
        } else {
            markPublished(notice, now);
        }
        notice.setCreatedBy(caller.userId());
        notice.setUpdatedBy(caller.userId());

        Notice saved = noticeRepo.save(notice);
        log.info("Notice {} created as {} by {}", saved.getId(), saved.getStatus(), caller.userId());
        return noticeMapper.convertToResponse(saved, now);
    }

    @Transactional(readOnly = true)
    public List<NoticeResponse> getAllNotices(NoticeStatus statusFilter) {
        Instant now = clock.instant();
        return noticeMapper.convertToResponseList(noticeRepo.findAllByOrderByIdDesc(), now).stream()
                .filter(response -> statusFilter == null || response.getStatus() == statusFilter)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public NoticeResponse getNotice(Long id) {
        return noticeMapper.convertToResponse(findOrThrow(id), clock.instant());
    }

    @Transactional
    public NoticeResponse updateNotice(Long id, NoticeRequest request, NoticeCaller caller) {
        Instant now = clock.instant();
        Notice notice = findOrThrow(id);
        NoticeStatus current = NoticeLifecycle.effectiveStatus(notice, now);
        if (!NoticeLifecycle.isEditable(notice, now)) {
            throw conflict("Only DRAFT or SCHEDULED notices can be edited; this notice is " + current);
        }
        validateDates(request.getPublishAt(), request.getExpiresAt(), now);

        noticeMapper.applyRequest(request, notice);
        if (current == NoticeStatus.SCHEDULED) {
            // Re-evaluate: moving publishAt to now/past publishes it.
            markPublished(notice, now);
        }
        notice.setUpdatedBy(caller.userId());
        return noticeMapper.convertToResponse(noticeRepo.save(notice), now);
    }

    /** DRAFT -> SCHEDULED/PUBLISHED depending on publishAt; SCHEDULED -> PUBLISHED immediately. */
    @Transactional
    public NoticeResponse publishNotice(Long id, NoticeCaller caller) {
        Instant now = clock.instant();
        Notice notice = findOrThrow(id);
        NoticeStatus current = NoticeLifecycle.effectiveStatus(notice, now);
        if (current == NoticeStatus.SCHEDULED) {
            notice.setPublishAt(now);
        } else if (current != NoticeStatus.DRAFT) {
            throw conflict("Only DRAFT or SCHEDULED notices can be published; this notice is " + current);
        }
        // A draft may have been saved long ago; its expiry may have passed since.
        validateDates(notice.getPublishAt(), notice.getExpiresAt(), now);
        markPublished(notice, now);
        notice.setUpdatedBy(caller.userId());
        log.info("Notice {} moved to {} by {}", id, notice.getStatus(), caller.userId());
        return noticeMapper.convertToResponse(noticeRepo.save(notice), now);
    }

    @Transactional
    public NoticeResponse archiveNotice(Long id, NoticeCaller caller) {
        Instant now = clock.instant();
        Notice notice = findOrThrow(id);
        if (notice.getStatus() == NoticeStatus.ARCHIVED) {
            throw conflict("Notice is already archived");
        }
        notice.setStatus(NoticeStatus.ARCHIVED);
        notice.setUpdatedBy(caller.userId());
        log.info("Notice {} archived by {}", id, caller.userId());
        return noticeMapper.convertToResponse(noticeRepo.save(notice), now);
    }

    /** Hard delete is allowed for drafts only; anything employees may have seen must be archived instead. */
    @Transactional
    public void deleteNotice(Long id, NoticeCaller caller) {
        Notice notice = findOrThrow(id);
        if (notice.getStatus() != NoticeStatus.DRAFT) {
            throw conflict("Only DRAFT notices can be deleted; archive it instead");
        }
        noticeRepo.delete(notice);
        log.info("Draft notice {} deleted by {}", id, caller.userId());
    }

    // ---------- Employee ----------

    @Transactional(readOnly = true)
    public List<NoticeResponse> getVisibleNotices() {
        Instant now = clock.instant();
        return noticeRepo.findVisible(NoticeLifecycle.LIVE_STATUSES, now).stream()
                .map(notice -> toEmployeeResponse(notice, now))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public NoticeResponse getVisibleNotice(Long id) {
        Instant now = clock.instant();
        return toEmployeeResponse(findVisibleOrThrow(id, now), now);
    }

    // ---------- Scheduler ----------

    /** Moves stored statuses forward for the current tenant. Reads are already correct without this. */
    @Transactional
    public TransitionResult applyScheduledTransitions() {
        Instant now = clock.instant();
        LocalDate today = LocalDate.now(clock);
        int published = noticeRepo.publishDue(NoticeStatus.SCHEDULED, NoticeStatus.PUBLISHED, now, today);
        int expired = noticeRepo.expireDue(NoticeLifecycle.LIVE_STATUSES, NoticeStatus.EXPIRED, now, today);
        return new TransitionResult(published, expired);
    }

    // ---------- Helpers ----------

    private NoticeResponse toEmployeeResponse(Notice notice, Instant now) {
        NoticeResponse response = noticeMapper.convertToResponse(notice, now);
        // Audit fields are for admins only.
        response.setCreatedBy(null);
        response.setUpdatedBy(null);
        return response;
    }

    /** Sets SCHEDULED or PUBLISHED from publishAt; a missing or past publishAt becomes now. */
    private static void markPublished(Notice notice, Instant now) {
        if (notice.getPublishAt() == null || notice.getPublishAt().isBefore(now)) {
            notice.setPublishAt(now);
        }
        notice.setStatus(NoticeLifecycle.statusOnPublish(notice.getPublishAt(), now));
    }

    private static void validateDates(Instant publishAt, Instant expiresAt, Instant now) {
        if (expiresAt == null) {
            return;
        }
        if (!expiresAt.isAfter(now)) {
            throw badRequest("expiresAt must be in the future");
        }
        if (publishAt != null && !expiresAt.isAfter(publishAt)) {
            throw badRequest("expiresAt must be after publishAt");
        }
    }

    private Notice findOrThrow(Long id) {
        return noticeRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Notice " + id + " not found"));
    }

    /** Hidden notices return 404, not 403, so employees cannot probe for drafts. */
    private Notice findVisibleOrThrow(Long id, Instant now) {
        return noticeRepo.findVisibleById(id, NoticeLifecycle.LIVE_STATUSES, now)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Notice " + id + " not found"));
    }

    private static ResponseStatusException conflict(String message) {
        return new ResponseStatusException(HttpStatus.CONFLICT, message);
    }

    private static ResponseStatusException badRequest(String message) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
    }
}
