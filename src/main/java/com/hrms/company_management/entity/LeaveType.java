package com.hrms.company_management.entity;

import com.hrms.company_management.utility.DisbursalFrequency;
import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "leave_types")
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
}