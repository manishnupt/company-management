package com.hrms.company_management.utility;

import java.time.Instant;
import java.util.EnumSet;
import java.util.Set;

import com.hrms.company_management.entity.Notice;
import com.hrms.company_management.entity.NoticeStatus;

/** Pure lifecycle rules for notices. No Spring, no database. */
public final class NoticeLifecycle {

    /** Stored statuses that can be visible to employees, depending on publishAt / expiresAt. */
    public static final Set<NoticeStatus> LIVE_STATUSES = EnumSet.of(NoticeStatus.SCHEDULED, NoticeStatus.PUBLISHED);

    private NoticeLifecycle() {
    }

    /** Status to store when a notice is published (or created without saveAsDraft). */
    public static NoticeStatus statusOnPublish(Instant publishAt, Instant now) {
        return publishAt != null && publishAt.isAfter(now) ? NoticeStatus.SCHEDULED : NoticeStatus.PUBLISHED;
    }

    /**
     * The status as of {@code now}. The scheduler moves stored statuses forward periodically;
     * this makes reads correct between scheduler runs and for legacy rows with a null status.
     */
    public static NoticeStatus effectiveStatus(Notice notice, Instant now) {
        NoticeStatus stored = notice.getStatus() == null ? NoticeStatus.PUBLISHED : notice.getStatus();
        if (!LIVE_STATUSES.contains(stored)) {
            return stored;
        }
        if (notice.getExpiresAt() != null && !notice.getExpiresAt().isAfter(now)) {
            return NoticeStatus.EXPIRED;
        }
        if (notice.getPublishAt() != null && notice.getPublishAt().isAfter(now)) {
            return NoticeStatus.SCHEDULED;
        }
        return NoticeStatus.PUBLISHED;
    }

    public static boolean isVisible(Notice notice, Instant now) {
        return effectiveStatus(notice, now) == NoticeStatus.PUBLISHED;
    }

    /** Only notices nobody can see yet may be edited. */
    public static boolean isEditable(Notice notice, Instant now) {
        NoticeStatus status = effectiveStatus(notice, now);
        return status == NoticeStatus.DRAFT || status == NoticeStatus.SCHEDULED;
    }
}
