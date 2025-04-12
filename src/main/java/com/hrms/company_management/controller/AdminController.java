package com.hrms.company_management.controller;

import com.hrms.company_management.dto.AssignRolesRequest;
import com.hrms.company_management.dto.GroupResponse;
import com.hrms.company_management.dto.RolesRequest;
import com.hrms.company_management.entity.RoleGroup;
import com.hrms.company_management.service.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/admin")
@CrossOrigin(origins="*")
public class AdminController {

    @Autowired
    AdminService adminService;

    @GetMapping("/role")
    public Map<String,String> getRoles(){
        return adminService.getRoles();
    }

    @PostMapping("/group")
    public String createGroup(@RequestParam String groupName,@RequestParam String groupDescription){
        return adminService.createGroup(groupName,groupDescription);
    }


    @GetMapping("/group")
    public List<GroupResponse> getAllGroups(){
        return  adminService.getAllGroups();
    }
    @GetMapping("/group/{groupId}/roles")
    public Map<String,String> getAllRolesByGroups(@PathVariable Long groupId){
        return  adminService.getAllRolesByGroup(groupId);
    }

    @GetMapping("/group/{groupId}")
    public RoleGroup getGroupById(@PathVariable Long groupId){
        return  adminService.getGroupById(groupId);
    }

    @PostMapping("group/{groupId}/roles")
    public ResponseEntity<RoleGroup> assignRolesToGroup(
            @PathVariable Long groupId,
            @RequestBody AssignRolesRequest request) {

        RoleGroup updatedGroup = adminService.assignRoles(groupId, request.getRoleNames());
        return ResponseEntity.ok(updatedGroup);
    }

    @PostMapping
    public ResponseEntity<Set<String>> getModulesForRoles(@RequestBody RolesRequest rolesRequest) {

        Set<String> modules= adminService.getModulesForRoles(rolesRequest);

        return ResponseEntity.ok(modules);
    }
}
