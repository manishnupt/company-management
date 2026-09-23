package com.hrms.company_management.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class AssignPolicyRequest {

    @NotNull
    private UUID policyId;
}
