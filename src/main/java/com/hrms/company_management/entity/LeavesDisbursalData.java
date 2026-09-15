package com.hrms.company_management.entity;

import com.hrms.company_management.utility.DisbursalFrequency;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class LeavesDisbursalData {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private int totalDays;
    private boolean carryForward;
    @Enumerated(EnumType.STRING)
    private DisbursalFrequency disbursalFrequency;
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "leave_type_id", nullable = false)
    private LeaveType leaveType;


    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
