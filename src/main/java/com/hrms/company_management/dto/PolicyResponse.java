package com.hrms.company_management.dto;

import com.hrms.company_management.utility.PolicyStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
public class PolicyResponse {

    private UUID id;

    private String name;

    private String description;

    private Integer version;

    private UUID rootPolicyId;

    private PolicyStatus status;

    private LocalDate effectiveFrom;

    private LocalDate effectiveTo;
}
