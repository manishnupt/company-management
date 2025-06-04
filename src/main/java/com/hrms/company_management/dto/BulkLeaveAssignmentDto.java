package com.hrms.company_management.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class BulkLeaveAssignmentDto {
    @NotBlank(message = "Leave type ID is required")
    private String leaveTypeId;

    @Min(value = 0, message = "Days must be non-negative")
    private int days;

    private String reason;

    private boolean overrideExisting = false;
}
