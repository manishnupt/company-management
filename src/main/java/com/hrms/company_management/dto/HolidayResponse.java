package com.hrms.company_management.dto;

import lombok.Data;

@Data
public class HolidayResponse {

    private Long id;
    private String name;
    private String date;
    private String type;
}
