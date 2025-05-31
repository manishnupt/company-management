package com.hrms.company_management.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDate;

@Data
public class HolidayRequest {

    private String name;
    @JsonFormat(pattern = "yyyy-MM-dd") // Helps Jackson parse it from JSON
    private LocalDate date;
    private String type;

}
