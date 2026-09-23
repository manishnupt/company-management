package com.hrms.company_management.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.Set;
import java.util.UUID;

@Data
@Builder
public class WorkScheduleResponse {

    private UUID id;

    private UUID policyId;

    @JsonFormat(pattern = "HH:mm")
    private LocalTime checkInTime;

    @JsonFormat(pattern = "HH:mm")
    private LocalTime checkOutTime;

    private BigDecimal minimumWorkingHours;

    private Set<DayOfWeek> workingDays;
}
