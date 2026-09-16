package com.hrms.company_management.dto;

import com.hrms.company_management.utility.DisbursalFrequency;
import lombok.Data;

@Data
public class WFHResponse {

    private String id;
    private String name;
    private String description;
    private int totalDays;
    private DisbursalFrequency disbursalFrequency;

}
