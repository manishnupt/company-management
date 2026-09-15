package com.hrms.company_management.dto;


import com.hrms.company_management.entity.LeaveType;
import com.hrms.company_management.utility.DisbursalFrequency;
import jakarta.persistence.*;
import lombok.Data;

import java.util.List;

@Data
public class LeavesDisbursalDto {
    private Long id;
    private int totalDays;
    private boolean carryForward;
    @Enumerated(EnumType.STRING)
    private DisbursalFrequency disbursalFrequency;
    private String description;
    private List<Long> groupId;
    private List<Long> leaveTypeId;
}
