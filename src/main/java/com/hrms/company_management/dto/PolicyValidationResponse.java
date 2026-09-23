package com.hrms.company_management.dto;

import com.hrms.company_management.utility.PolicyStatus;
import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
@Builder
public class PolicyValidationResponse {

    private UUID policyId;

    private PolicyStatus status;

    // true when there are no errors; warnings do not block publishing
    private boolean valid;

    private List<String> errors;

    private List<String> warnings;
}
