package com.hrms.company_management.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.Set;

@Data
public class WorkScheduleRequest {

    @NotNull
    @JsonFormat(pattern = "HH:mm")
    private LocalTime checkInTime;

    @NotNull
    @JsonFormat(pattern = "HH:mm")
    private LocalTime checkOutTime;

    @NotNull
    @DecimalMin(value = "0.0", inclusive = false)
    private BigDecimal minimumWorkingHours;

    @NotEmpty
    private Set<DayOfWeek> workingDays;
}
