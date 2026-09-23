package com.hrms.company_management.entity;

import com.hrms.company_management.utility.PolicyStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
    name = "policies",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_policy_name_version",
            columnNames = {"name", "version"}
        )
    }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Policy {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private Integer version = 1;

    // shared by all versions of the same policy; equals id for version 1
    @Column(name = "root_policy_id")
    private UUID rootPolicyId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PolicyStatus status;

    private LocalDate effectiveFrom;

    private LocalDate effectiveTo;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private String createdBy;

    private LocalDateTime updatedAt;

    private String updatedBy;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}