package com.hrms.company_management.dto;

import com.hrms.company_management.utility.DisbursalFrequency;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateWfhTypeRequest {
    @NotBlank
    private String name;

    private String description;

    @NotNull
    @Min(0)
    private Integer totalDays;

    @NotNull
    private DisbursalFrequency disbursalFrequency;
}
