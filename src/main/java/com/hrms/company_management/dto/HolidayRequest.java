package com.hrms.company_management.dto;

import lombok.Data;

@Data
public class HolidayRequest {

    private String name;
    private String date;
    private String type;

}
