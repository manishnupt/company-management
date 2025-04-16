package com.hrms.company_management.service;

import com.hrms.company_management.dto.GenerateTokenRequest;
import com.hrms.company_management.dto.GroupResponse;
import com.hrms.company_management.dto.RolesRequest;
import com.hrms.company_management.entity.Role;
import com.hrms.company_management.entity.RoleGroup;
import com.hrms.company_management.repository.RoleGroupRepo;
import com.hrms.company_management.repository.RoleRepo;
import com.hrms.company_management.utility.CompanyManagementHelper;
import com.hrms.company_management.utility.Constants;
import com.hrms.company_management.utility.TenantContext;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Log4j2
public class AdminService {

    @Autowired
    RoleRepo roleRepo;

    @Autowired
    RoleGroupRepo roleGroupRepo;

    @Autowired
    private RestTemplate restTemplate;

    @Value("${tenant_management_base_url}")
    private String tenantUrl;

    @Value("${iam_service_base_url}")
    private String iamServiceBaseUrl;




    public Map<String, String> getRoles() {
        List<Role> allRoles = roleRepo.findAll();
        Map<String, String> hm = new HashMap<>();
        for (Role role : allRoles) {
            hm.put(role.getName(), role.getDescription());
        }
        return hm;
    }

    public String createGroup(String groupName, String groupDescription) {

        if (roleGroupRepo.findByName(groupName).isPresent()) {
            throw new RuntimeException("Group already exists");
        }
        RoleGroup roleGroup = new RoleGroup();
        roleGroup.setName(groupName);
        roleGroup.setDescription(groupDescription);
        RoleGroup savedGroup = roleGroupRepo.save(roleGroup);
        handleKeycloakGroupCreation(groupName, TenantContext.getCurrentTenant());
        if (savedGroup.getId() != null) {
            return "Group saved success";
        }
        return "Facing issues in creating group";
    }

    private void handleKeycloakGroupCreation(String groupName,String tenant) {
        Map<String,Object> masterRealmDetails =getMasterRealmDetails();
        String token=getKeycloakToken(masterRealmDetails);
        log.info("token:{}",token);
        String groupInKeycloak = createGroupInKeycloak(tenant, groupName, token);
        System.out.println(groupInKeycloak);
    }

    private String createGroupInKeycloak(String realm, String group, String token) {
            HttpHeaders headers = createHeaders(token);
            String createGroupUrl = iamServiceBaseUrl + Constants.CREATE_GROUP + "?groupName=" + URLEncoder.encode(group, StandardCharsets.UTF_8) + "&realmName=" + URLEncoder.encode(realm, StandardCharsets.UTF_8);
            log.info("createGroupUrl:{}",createGroupUrl);
            HttpEntity<String> requestEntity = new HttpEntity<>(headers);
            ResponseEntity<String> exchange = restTemplate.exchange(createGroupUrl, HttpMethod.POST, requestEntity, String.class);
            return String.valueOf(exchange.getBody());
    }

    public String getKeycloakToken(Map<String,Object> masterRealmDetails) {
        String adminAccessTokenUrl = iamServiceBaseUrl + Constants.GET_ADMIN_TOKEN;
        GenerateTokenRequest tokenRequest = CompanyManagementHelper.getGenerateTokenRequest(masterRealmDetails);
        HttpEntity<GenerateTokenRequest> entity = new HttpEntity<>(tokenRequest, createHeaders(null));

        ResponseEntity<Map<String, String>> response = makeApiCall(adminAccessTokenUrl, HttpMethod.POST, entity,
                new ParameterizedTypeReference<>() {}, 500, "Failed to obtain admin access token");
        log.info("fetched the admin token succesfully");

        return response.getBody().get("token");
        }


    private Map<String, Object> getMasterRealmDetails() {
        String url = tenantUrl +"/api/v1/tenants/auth-config/" + "master";
        ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);
        Map<String, Object> responseBody = response.getBody();
        return (Map<String, Object>) responseBody.get("data");
    }

    public List<GroupResponse> getAllGroups() {
        List<RoleGroup> allGroupRoles = roleGroupRepo.findAll();
        List<GroupResponse> groupResponses = new ArrayList<>();
        for (RoleGroup roleGroup : allGroupRoles) {
            GroupResponse response = new GroupResponse();
            response.setGroupId(roleGroup.getId());
            response.setGroupName(roleGroup.getName());
            // response.setRoles(roleGroup.getRoles());
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

    public Map<String, String> getAllRolesByGroup(Long groupId) {
        RoleGroup groupById = roleGroupRepo.findById(groupId).get();
        Set<Role> roles = groupById.getRoles();
        Map<String, String> hm = new HashMap<>();
        for (Role role : roles) {
            hm.put(role.getName(), role.getDescription());
        }
        return hm;
    }

    public RoleGroup getGroupById(Long groupId) {
        RoleGroup group = roleGroupRepo.findById(groupId).get();
        GroupResponse response = new GroupResponse();
        response.setGroupId(group.getId());
        response.setGroupName(group.getName());
        // response.setRoles(roleGroup.getRoles());
        response.setGroupDescription(group.getDescription());
        return group;

    }

    public Set<String> getModulesForRoles(RolesRequest rolesRequest) {
        Set<String> roles =rolesRequest.getRoles();
        List<Role> allRoles = roleRepo.findAll();
        log.info("total roles count:{}",allRoles.size());
        Set<String> modules = new HashSet<>();
        for(Role role:allRoles){
            if(roles.contains(role.getName())) {
                log.info("role present adding to modules list :{]",role.getModule());
                modules.add(role.getModule());
            }
        }
        return  modules;
    }

    public Set<String> getRolesByModules(List<String> moduleNames) {
        List<Role> allRoles = roleRepo.findByModuleIn(moduleNames);
        Set<String> roles = new HashSet<>();
        for(Role role:allRoles){
            roles.add(role.getName());
        }
        return roles;
    }

    private HttpHeaders createHeaders(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

    private <T> ResponseEntity<T> makeApiCall(String url, HttpMethod method, HttpEntity<?> entity, ParameterizedTypeReference<T> responseType, int errorCode, String errorMessage) {
        try {
            return restTemplate.exchange(url, method, entity, responseType);
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            log.error("{} - Status: {}, Response: {}", errorMessage, e.getStatusCode(), e.getResponseBodyAsString(), e);
            throw new RuntimeException(errorMessage + " - Status: " + e.getStatusCode(), e);
        }
    }
}