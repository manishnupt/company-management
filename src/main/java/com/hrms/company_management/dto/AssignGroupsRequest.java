package com.hrms.company_management.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class AssignGroupsRequest {

    @NotEmpty
    private List<Long> groupIds;
}
