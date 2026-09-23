package com.hrms.company_management.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class AttendanceRuleRequest {

    @NotNull
    @Min(0)
    private Integer lateMarkThresholdMinutes;

    @NotNull
    @Min(0)
    private Integer consecutiveLateDaysAllowed;

    @NotNull
    @DecimalMin(value = "0.0", inclusive = false)
    private BigDecimal autoMarkAbsentAfterHours;
}
