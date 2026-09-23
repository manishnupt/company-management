package com.hrms.company_management.dto;

import com.hrms.company_management.utility.DressCode;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class MiscellaneousRequest {

    @NotNull
    private DressCode dressCode;
}
