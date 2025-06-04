package com.hrms.company_management.entity;

import com.hrms.company_management.utility.DisbursalFrequency;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "leave_types")
@Data
public class LeaveType {
    @Id
    private String id;

    private String name;
    private int totalDays;
    private boolean carryForward;
    private int maxCarryForwardDays;
    private int defaultTotalDays;

    @Enumerated(EnumType.STRING)
    private DisbursalFrequency disbursalFrequency;

    private String description;

    private boolean isActive = true;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate(){
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected  void  onUpdate(){
        updatedAt = LocalDateTime.now();
    }
}