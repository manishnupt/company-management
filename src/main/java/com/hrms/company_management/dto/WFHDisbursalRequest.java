package com.hrms.company_management.dto;

import com.hrms.company_management.utility.DisbursalFrequency;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Data;

import java.util.List;

@Data
public class WFHDisbursalRequest {
    private Long id;
    private int totalDays;
    private boolean expireUnusedDaysAtCycleEnd;
    @Enumerated(EnumType.STRING)
    private DisbursalFrequency disbursalFrequency;
    private String description;
    private List<Long> groupId;
}
