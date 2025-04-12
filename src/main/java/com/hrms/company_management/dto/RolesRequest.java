package com.hrms.company_management.dto;

import lombok.Data;

import java.util.Set;

@Data
public class RolesRequest {
    private Set<String> roles;
}
