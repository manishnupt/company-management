package com.hrms.company_management.dto;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class MiscellaneousResponse {

    private UUID id;

    private UUID policyId;

    private String dressCode;
}
