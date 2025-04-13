package com.hrms.company_management.dto;

import lombok.Data;

import java.util.List;

@Data
public class ModulesRequest {
    private List<String> moduleNames;
}
