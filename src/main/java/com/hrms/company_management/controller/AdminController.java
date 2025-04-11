package com.hrms.company_management.controller;

import com.hrms.company_management.dto.AssignRolesRequest;
import com.hrms.company_management.dto.GroupResponse;
import com.hrms.company_management.entity.RoleGroup;
import com.hrms.company_management.service.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    AdminService adminService;

    @GetMapping("/roles")
    public Map<String,String> getRoles(){
        return adminService.getRoles();
    }

    @PostMapping("/createGroup")
    public String createGroup(@RequestParam String groupName,@RequestParam String groupDescription){
        return adminService.createGroup(groupName,groupDescription);
    }


    @GetMapping("/groups")
    public List<GroupResponse> getAllGroups(){
        return  adminService.getAllGroups();
    }
    @GetMapping("/group/{groupId}/roles")
    public Map<String,String> getAllRolesByGroups(@PathVariable Long groupId){
        return  adminService.getAllRolesByGroup(groupId);
    }

    @PostMapping("/{groupId}/roles")
    public ResponseEntity<RoleGroup> assignRolesToGroup(
            @PathVariable Long groupId,
            @RequestBody AssignRolesRequest request) {

        RoleGroup updatedGroup = adminService.assignRoles(groupId, request.getRoleNames());
        return ResponseEntity.ok(updatedGroup);
    }
}
