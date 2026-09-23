package com.hrms.company_management.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
public class AttendanceRuleResponse {

    private UUID id;

    private UUID policyId;

    private Integer lateMarkThresholdMinutes;

    private Integer consecutiveLateDaysAllowed;

    private BigDecimal autoMarkAbsentAfterHours;
}
