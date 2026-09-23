package com.hrms.company_management.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class UpdatePolicyRequest {

    @NotBlank
    private String name;

    private String description;

    @NotNull
    private LocalDate effectiveFrom;

    private LocalDate effectiveTo;
}
