package com.hrms.company_management.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class EmployeeLifecycleRequest {

    @NotNull
    @Min(0)
    private Integer probationPeriodDays;

    @NotNull
    @Min(0)
    private Integer noticePeriodDays;
}
