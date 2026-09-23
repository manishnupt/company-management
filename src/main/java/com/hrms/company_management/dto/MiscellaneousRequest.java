package com.hrms.company_management.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class MiscellaneousRequest {

    @Size(max = 500)
    private String dressCode;
}
