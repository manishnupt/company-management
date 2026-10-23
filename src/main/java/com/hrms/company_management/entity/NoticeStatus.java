package com.hrms.company_management.entity;

/**
 * Stored lifecycle state of a notice.
 * DRAFT -> SCHEDULED -> PUBLISHED -> EXPIRED, and any non-archived state -> ARCHIVED.
 */
public enum NoticeStatus {
    DRAFT,
    SCHEDULED,
    PUBLISHED,
    EXPIRED,
    ARCHIVED
}
