package com.hrms.company_management.dto;

import com.hrms.company_management.entity.Role;
import lombok.Data;

import java.util.Set;

@Data
public class GroupResponse {
    private long groupId;
    private String groupName;
    private String groupDescription;
   // private Set<Role> roles;
}
