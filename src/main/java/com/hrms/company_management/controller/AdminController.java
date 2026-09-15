package com.hrms.company_management.controller;

import com.hrms.company_management.dto.*;
import com.hrms.company_management.entity.RoleGroup;
import com.hrms.company_management.service.AdminService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


@RestController
@RequestMapping("/admin")
@CrossOrigin(origins="*")
@Log4j2
public class AdminController {

    @Autowired
    AdminService adminService;

    @GetMapping("/role")
    public List<GroupRolesResponse> getRoles(){
        log.info("getRoles called");
        return adminService.getRoles();
    }

    @PostMapping("/group")
    public String createGroup(@RequestParam String groupName,@RequestParam String groupDescription){
        log.info("createGroup called with groupName:{}, groupDescription:{}", groupName, groupDescription);
        return adminService.createGroup(groupName,groupDescription);
    }

    @PutMapping("/group/{groupId}")
    public ResponseEntity<RoleGroup> updateGroup(
            @PathVariable Long groupId,
            @RequestParam String groupName,
            @RequestParam String groupDescription) {

        log.info("updateGroup called with groupId:{}, groupName:{}, groupDescription:{}", groupId, groupName, groupDescription);
        RoleGroup updatedGroup = adminService.updateGroup(groupId, groupName, groupDescription);
        return ResponseEntity.ok(updatedGroup);
    }


    @GetMapping("/group")
    public List<GroupResponse> getAllGroups(){
        log.info("getAllGroups called");
        return  adminService.getAllGroups();
    }

    @GetMapping("/group/{groupId}/roles")
    public List<GroupRolesResponse> getAllRolesByGroups(@PathVariable Long groupId){
        log.info("getAllRolesByGroups called with groupId:{}", groupId);
        return  adminService.getAllRolesByGroup(groupId);
    }

    @GetMapping("/group/{groupId}")
    public RoleGroup getGroupById(@PathVariable Long groupId){
        log.info("getGroupById called with groupId:{}", groupId);
        return  adminService.getGroupById(groupId);
    }

    @PostMapping("group/{groupId}/roles")
    public ResponseEntity<RoleGroup> assignRolesToGroup(
            @PathVariable Long groupId,
            @RequestBody AssignRolesRequest request) {

        log.info("assignRolesToGroup called with groupId:{}, roleNames:{}", groupId, request.getRoleNames());
        RoleGroup updatedGroup = adminService.assignRoles(groupId, request.getRoleNames());
        return ResponseEntity.ok(updatedGroup);
    }
    @PostMapping("/modules")
    public ResponseEntity<Set<String>> getModulesForRoles(@RequestBody RolesRequest rolesRequest) {

        log.info("getModulesForRoles called with rolesRequest:{}", rolesRequest);
        Set<String> modules= adminService.getModulesForRoles(rolesRequest);

        return ResponseEntity.ok(modules);
    }
    @PostMapping("/modules/roles")
    public ResponseEntity<Set<String>> getRolesByModules(@RequestBody ModulesRequest request) {
        log.info("getRolesByModules called with moduleNames:{}", request.getModuleNames());
        Set<String> roles = adminService.getRolesByModules(request.getModuleNames());
        return ResponseEntity.ok(roles);
    }

    @PostMapping("/notice")
    public ResponseEntity<String> publishNotice(@RequestBody NoticeRequest noticeRequest) {
       log.info("publishNotice called with noticeRequest:{}", noticeRequest);
       return ResponseEntity.ok(adminService.publishNotice(noticeRequest));
    }

    @GetMapping("/notice")
    public ResponseEntity<List<NoticeResponse>> getAllNotices() {
        log.info("getAllNotices called");
        return ResponseEntity.ok(adminService.getAllNotices());
    }

    @PostMapping("/holiday")
    public ResponseEntity<String> addHoliday(@RequestBody HolidayRequest holidayRequest) {
        log.info("addHoliday called with holidayRequest:{}", holidayRequest);
        return ResponseEntity.ok(adminService.addHoliday(holidayRequest));
    }
    @GetMapping("/holiday")
    public ResponseEntity<List<HolidayResponse>> getAllHolidays() {
        log.info("getAllHolidays called");
        return ResponseEntity.ok(adminService.getAllHolidays());
    }

    @PutMapping("/holiday/{id}")
    public ResponseEntity<HolidayResponse> updateHoliday(@PathVariable Long id, @RequestBody HolidayRequest holidayRequest) {
        log.info("updateHoliday called with id:{}, holidayRequest:{}", id, holidayRequest);
        return ResponseEntity.ok(adminService.updateHoliday(id, holidayRequest));
    }

    @DeleteMapping("/holiday/{id}")
    public ResponseEntity<String> deleteHoliday(@PathVariable Long id) {
        log.info("deleteHoliday called with id:{}", id);
        return ResponseEntity.ok(adminService.deleteHoliday(id));
    }



    @PostMapping("/company-policy")
    public ResponseEntity<String> companyPolicy(@RequestBody Map<String, Object> request){
       log.info("companyPolicy called with request:{}", request);
       return adminService.companyPolicy(request);
    }

    @DeleteMapping("/group/{groupId}/roles/{roleId}")
    public ResponseEntity<String> removeRoleFromGroup(@PathVariable Long groupId, @PathVariable Long roleId) {
        log.info("removeRoleFromGroup called with groupId:{}, roleId:{}", groupId, roleId);
        String response = adminService.removeRoleFromGroup(groupId, roleId);
        return ResponseEntity.ok(response);
    }

}
