package com.hrms.company_management.dto;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class EmployeeLifecycleResponse {

    private UUID id;

    private UUID policyId;

    private Integer probationPeriodDays;

    private Integer noticePeriodDays;
}
