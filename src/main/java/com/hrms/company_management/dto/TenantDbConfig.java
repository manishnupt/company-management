package com.hrms.company_management.dto;

import lombok.Data;

@Data
public class TenantDbConfig {
    private String tenantId;
    private String dbUrl;
    private String username;
    private String password;
}

