package com.hrms.company_management.dto;

import com.hrms.company_management.utility.DisbursalFrequency;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class LeaveTypeResponse {

    private String id;
    private UUID policyId;
    private String name;
    private int totalDays;
    private boolean carryForward;
    private DisbursalFrequency disbursalFrequency;
    private String description;
}