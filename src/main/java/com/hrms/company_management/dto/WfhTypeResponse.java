package com.hrms.company_management.dto;
import com.hrms.company_management.utility.DisbursalFrequency;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class WfhTypeResponse {

    private String id;

    private UUID policyId;

    private String name;

    private String description;

    private int totalDays;

    private DisbursalFrequency disbursalFrequency;
}
