package com.hrms.company_management.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
    name = "policy_miscellaneous",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_policy_miscellaneous",
            columnNames = "policy_id"
        )
    }
)
@Data
public class PolicyMiscellaneous {

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

    @Column(length = 500)
    private String dressCode;

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