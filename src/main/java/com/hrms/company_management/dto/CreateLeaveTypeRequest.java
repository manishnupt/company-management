package com.hrms.company_management.dto;

import com.hrms.company_management.utility.DisbursalFrequency;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateLeaveTypeRequest {

    @NotBlank
    private String name;

    @Min(0)
    private int totalDays;

    private boolean carryForward;

    @NotNull
    private DisbursalFrequency disbursalFrequency;

    private String description;
}