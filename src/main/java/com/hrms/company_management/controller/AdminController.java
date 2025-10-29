package com.hrms.company_management.controller;

import com.hrms.company_management.dto.AssignRolesRequest;
import com.hrms.company_management.dto.GroupResponse;
import com.hrms.company_management.dto.HolidayRequest;
import com.hrms.company_management.dto.HolidayResponse;
import com.hrms.company_management.dto.ModulesRequest;
import com.hrms.company_management.dto.NoticeResponse;
import com.hrms.company_management.dto.NoticeRequest;
import com.hrms.company_management.dto.RolesRequest;
import com.hrms.company_management.entity.RoleGroup;
import com.hrms.company_management.service.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


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

    @PostMapping("group/{groupId}/remove-roles")
    public ResponseEntity<RoleGroup> removeRolesFromGroup(
            @PathVariable Long groupId,
            @RequestBody AssignRolesRequest request) {

        RoleGroup updatedGroup = adminService.removeRoles(groupId, request.getRoleNames());
        return ResponseEntity.ok(updatedGroup);
    }

    @PostMapping("/modules")
    public ResponseEntity<Set<String>> getModulesForRoles(@RequestBody RolesRequest rolesRequest) {

        Set<String> modules= adminService.getModulesForRoles(rolesRequest);

        return ResponseEntity.ok(modules);
    }
    @PostMapping("/modules/roles")
    public ResponseEntity<Set<String>> getRolesByModules(@RequestBody ModulesRequest request) {
        Set<String> roles = adminService.getRolesByModules(request.getModuleNames());
        return ResponseEntity.ok(roles);
    }
    
    @PostMapping("/notice")
    public ResponseEntity<String> publishNotice(@RequestBody NoticeRequest noticeRequest) {
       return ResponseEntity.ok(adminService.publishNotice(noticeRequest));
    }
    
    @GetMapping("/notice")
    public ResponseEntity<List<NoticeResponse>> getAllNotices() {
        return ResponseEntity.ok(adminService.getAllNotices());
    }

    @PostMapping("/holiday")
    public ResponseEntity<String> addHoliday(@RequestBody HolidayRequest holidayRequest) {
        return ResponseEntity.ok(adminService.addHoliday(holidayRequest));
    }
    @GetMapping("/holiday")
    public ResponseEntity<List<HolidayResponse>> getAllHolidays() {
        return ResponseEntity.ok(adminService.getAllHolidays());
    }

    @PostMapping("/company-policy")
    public ResponseEntity<String> companyPolicy(@RequestBody Map<String, Object> request){
       return adminService.companyPolicy(request);
    }

}
