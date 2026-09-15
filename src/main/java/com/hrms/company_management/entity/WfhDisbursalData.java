package com.hrms.company_management.entity;

import com.hrms.company_management.utility.DisbursalFrequency;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;


@Entity
@Data
public class WfhDisbursalData {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private int totalDays;
    private boolean expireUnusedDaysAtCycleEnd;
    @Enumerated(EnumType.STRING)
    private DisbursalFrequency disbursalFrequency;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id", nullable = false)
    private RoleGroup roleGroup;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;


    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
