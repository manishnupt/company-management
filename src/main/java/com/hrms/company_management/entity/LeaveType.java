package com.hrms.company_management.entity;

import com.hrms.company_management.utility.DisbursalFrequency;
import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(
    name = "leave_types",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_leave_type_policy_name",
            columnNames = {"policy_id", "name"}
        )
    }
)
@Data
public class LeaveType {
    @Id
    private String id;

    private String name;
    private int totalDays;
    private boolean carryForward;

    @Enumerated(EnumType.STRING)
    private DisbursalFrequency disbursalFrequency;

    private String description;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "policy_id", nullable = false)
    private Policy policy;
}