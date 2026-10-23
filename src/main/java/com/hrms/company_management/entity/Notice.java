package com.hrms.company_management.entity;

import java.time.Instant;
import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import lombok.Data;

/**
 * A tenant-wide notice. Every tenant has its own database, so the audience is everyone in that tenant.
 *
 * Rows created before the lifecycle columns existed have null status / publishAt.
 * They are treated as PUBLISHED and visible since creation (see NoticeLifecycle).
 */
@Entity
@Data
public class Notice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String title;
    private String description;
    private String noticeType;

    @Enumerated(EnumType.STRING)
    private NoticeStatus status;

    /** When the notice becomes visible. Null on legacy rows only. */
    private Instant publishAt;

    /** When the notice stops being visible. Null means it never expires. */
    private Instant expiresAt;

    @Column(updatable = false)
    private LocalDate createdAt;
    private LocalDate updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDate.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDate.now();
    }
}
