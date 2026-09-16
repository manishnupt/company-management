package com.hrms.company_management.dto;

import com.hrms.company_management.utility.DisbursalFrequency;
import lombok.Data;

@Data
public class WFHRequest {

    private String name;
    private String description;
    private int totalDays;
    private DisbursalFrequency disbursalFrequency;

}
