package com.hrms.company_management.dto;

import com.hrms.company_management.utility.DisbursalFrequency;
import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class LeaveTypeDto {
    @NotBlank(message = "Leave type name is required")
    @Size(min = 2, max = 100, message = "Leave type name must be between 2 and 100 characters")
    private String name;

    @Min(value = 0, message = "Default total days must be non-negative")
    @Max(value = 365, message = "Default total days cannot exceed 365")
    private int defaultTotalDays;

    private boolean carryForward;

    @Min(value = 0, message = "Max carry forward days must be non-negative")
    private int maxCarryForwardDays;

    @NotNull(message = "Disbursal frequency is required")
    private DisbursalFrequency disbursalFrequency;

    @Size(max = 500, message = "Description cannot exceed 500 characters")
    private String description;
}
