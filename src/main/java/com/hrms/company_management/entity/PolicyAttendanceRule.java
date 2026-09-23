package com.hrms.company_management.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;


@Entity
@Table(
    name = "policy_attendance_rules",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_policy_attendance_rule",
            columnNames = "policy_id"
        )
    }
)
@Data
public class PolicyAttendanceRule {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
        name = "policy_id",
        nullable = false,
        unique = true
    )
    private Policy policy;

    @Column(nullable = false)
    private Integer lateMarkThresholdMinutes;

    @Column(nullable = false)
    private Integer consecutiveLateDaysAllowed;

    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal autoMarkAbsentAfterHours;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}