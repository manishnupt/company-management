package com.hrms.company_management.service;

import com.hrms.company_management.dto.GroupResponse;
import com.hrms.company_management.entity.Role;
import com.hrms.company_management.entity.RoleGroup;
import com.hrms.company_management.repository.RoleGroupRepo;
import com.hrms.company_management.repository.RoleRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class AdminService {

    @Autowired
    RoleRepo roleRepo;

    @Autowired
    RoleGroupRepo roleGroupRepo;


    public Map<String, String> getRoles() {
        List<Role> allRoles = roleRepo.findAll();
        Map<String,String> hm = new HashMap<>();
        for(Role role:allRoles){
            hm.put(role.getName(), role.getDescription());
        }
        return hm;
    }

    public String createGroup(String groupName,String groupDescription) {

        if (roleGroupRepo.findByName(groupName).isPresent()) {
            throw new RuntimeException("Group already exists");
        }
        RoleGroup roleGroup = new RoleGroup();
        roleGroup.setName(groupName);
        roleGroup.setDescription(groupDescription);
        RoleGroup savedGroup = roleGroupRepo.save(roleGroup);
        if (savedGroup.getId()!=null){
            return "Group saved success";
        }
        return "Facing issues in creating group";
    }

    public List<GroupResponse> getAllGroups() {
        List<RoleGroup> allGroupRoles = roleGroupRepo.findAll();
        List<GroupResponse> groupResponses= new ArrayList<>();
        for(RoleGroup roleGroup:allGroupRoles){
            GroupResponse response = new GroupResponse();
            response.setGroupId(roleGroup.getId());
            response.setGroupName(roleGroup.getName());
            response.setRoles(roleGroup.getRoles());
            response.setGroupDescription(roleGroup.getDescription());
            groupResponses.add(response);
        }
        return groupResponses;
    }
    public RoleGroup assignRoles(Long groupId, List<String> roleNames) {
        RoleGroup group = roleGroupRepo.findById(groupId)
                .orElseThrow(() -> new RuntimeException("Group not found"));

        Set<Role> roles = roleNames.stream()
                .map(roleName -> roleRepo.findByName(roleName)
                        .orElseThrow(() -> new RuntimeException("Role not found: " + roleName)))
                .collect(Collectors.toSet());

        group.getRoles().addAll(roles);
        return roleGroupRepo.save(group);
    }
}
