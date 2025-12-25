package com.hrms.company_management.dto;

import lombok.Data;

@Data
public class GroupRolesResponse {
    private long groupRoleId;
    private String roleName;
    private String roleDescription;
}
