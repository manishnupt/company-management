package com.hrms.company_management.service;

import com.hrms.company_management.dto.GenerateTokenRequest;
import com.hrms.company_management.dto.GroupResponse;
import com.hrms.company_management.dto.HolidayRequest;
import com.hrms.company_management.dto.HolidayResponse;
import com.hrms.company_management.dto.NoticeResponse;
import com.hrms.company_management.dto.NoticeRequest;
import com.hrms.company_management.dto.RolesRequest;
import com.hrms.company_management.entity.*;
import com.hrms.company_management.repository.*;
import com.hrms.company_management.utility.CompanyManagementHelper;
import com.hrms.company_management.utility.Constants;
import com.hrms.company_management.utility.HolidayMapper;
import com.hrms.company_management.utility.NoticeMapper;
import com.hrms.company_management.utility.TenantContext;
import com.hrms.company_management.utility.PolicyHelper;
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

    @Autowired
    NoticeRepo noticeRepository;

    @Autowired
    HolidayRepo holidayRepository;

    @Autowired
    NoticeMapper noticeMapper;

    @Autowired
    HolidayMapper holidayMapper;

    @Autowired
    private PolicyRepo policyRepo;

    @Autowired
    private PolicyAttributeRepository policyAttributeRepository;

    @Value("${tenant_management_base_url}")
    private String tenantUrl;

    @Value("${iam_service_base_url}")
    private String iamServiceBaseUrl;

    @Autowired
    private AttributeValueRepository attributeValueRepository;




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
        String keycloakGroupId = handleKeycloakGroupCreation(groupName, TenantContext.getCurrentTenant());
        roleGroup.setKcGroupIdRef(keycloakGroupId);
        RoleGroup savedGroup = roleGroupRepo.save(roleGroup);
        if (savedGroup.getId() != null) {
            return "Group saved success";
        }
        return "Facing issues in creating group";
    }

    private String handleKeycloakGroupCreation(String groupName,String tenant) {
        Map<String,Object> masterRealmDetails =getMasterRealmDetails();
        String token=getKeycloakToken(masterRealmDetails);
        log.info("token:{}",token);
        return createGroupInKeycloak(tenant, groupName, token);
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

        handleKeycloakGroupRoleAssignment(group.getKcGroupIdRef(),roleNames,TenantContext.getCurrentTenant());

        group.getRoles().addAll(roles);
        return roleGroupRepo.save(group);
    }

    private void handleKeycloakGroupRoleAssignment(String id, List<String> roleNames, String currentTenant) {

        Map<String,Object> masterRealmDetails =getMasterRealmDetails();
        String token=getKeycloakToken(masterRealmDetails);
        log.info("token:{}",token);
        assignRoleToGroupKC(token,id,roleNames,currentTenant);
    }

    private void assignRoleToGroupKC(String token, String id, List<String> roleNames, String currentTenant) {
        try {
            // Create RestTemplate instance
            RestTemplate restTemplate = new RestTemplate();

            String url = iamServiceBaseUrl + "/assign-group-roles?groupId=" + id + "&realmName=" + currentTenant;

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", token);

            HttpEntity<List<String>> requestEntity = new HttpEntity<>(roleNames, headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    requestEntity,
                    String.class
            );

            // Handle the response
            if (response.getStatusCode() == HttpStatus.OK) {
                System.out.println("Roles assigned successfully: " + response.getBody());
            } else {
                System.out.println("Failed to assign roles. Status code: " + response.getStatusCode());
            }
        } catch (Exception e) {
            System.err.println("Error calling assign-group-roles API: " + e.getMessage());
            e.printStackTrace();
        }


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

    // Notice Management
    public String publishNotice(NoticeRequest noticeRequest) {

        Notice notice = noticeMapper.convertToEntity(noticeRequest);
        Notice savedNotice = noticeRepository.save(notice);
        if (savedNotice.getId() != null) {
            return "Notice published successfully";
        } else {
            return "Failed to publish notice";
        }
    }

    public List<NoticeResponse> getAllNotices() {

        List<Notice> notices = noticeRepository.findAll();
        if (notices.isEmpty()) {
            throw new RuntimeException("No notices found");
        }
        List<NoticeResponse> noticeResponses = noticeMapper.convertToResponseList(notices);
        return noticeResponses;

    }

    public String addHoliday(HolidayRequest holidayRequest) {

        Holiday holiday = holidayMapper.convertToEntity(holidayRequest);
        Holiday savedHoliday = holidayRepository.save(holiday);
        if (savedHoliday.getId() != null) {
            return "Holiday added successfully";
        } else {
            return "Failed to add holiday";
        }
    }

    public List<HolidayResponse> getAllHolidays() {

        List<Holiday> holidays = holidayRepository.findAll();
        if (holidays.isEmpty()) {
            throw new RuntimeException("No holidays found");
        }
        List<HolidayResponse> holidayResponses = holidayMapper.convertToResponseList(holidays);
        return holidayResponses;
    }

    public ResponseEntity<String> companyPolicy(Map<String, Object> request) {
        log.info("Received request for company policy update: {}", request);

        Map<String, Object> policyRules = (Map<String, Object>) request.get("policyRules");
        if (policyRules == null) {
            log.warn("No 'policyRules' key found in request.");
            return ResponseEntity.badRequest().body("'policyRules' is required.");
        }

        Map<String, Map<String, String>> policyData = PolicyHelper.extractPolicyData(policyRules);
        log.info("Extracted policy data: {}", policyData);

        boolean hasAnyChange = false;
        int latestVersion = attributeValueRepository.findMaxVersion().orElse(0);
        int newVersion = latestVersion + 1;
        log.info("Latest version: {} | New version to apply: {}", latestVersion, newVersion);

        List<AttributeValue> newAttributeValues = new ArrayList<>();

        for (Map.Entry<String, Map<String, String>> policyEntry : policyData.entrySet()) {
            String policyName = policyEntry.getKey();
            Map<String, String> attributes = policyEntry.getValue();
            log.info("Processing policy: {}", policyName);

            // Get or create Policy
            Policy policy = policyRepo.findByPolicyName(policyName)
                    .orElseGet(() -> {
                        log.info("Policy '{}' not found. Creating new one.", policyName);
                        Policy p = new Policy();
                        p.setPolicyName(policyName);
                        return policyRepo.save(p);
                    });

            for (Map.Entry<String, String> attrEntry : attributes.entrySet()) {
                String attrName = attrEntry.getKey();
                String newValue = attrEntry.getValue();
                log.info("Processing attribute '{}' with new value '{}'", attrName, newValue);

                // Get or create PolicyAttribute
                PolicyAttribute attribute = policyAttributeRepository.findByPolicyAndAttributeName(policy, attrName)
                        .orElseGet(() -> {
                            log.info("Attribute '{}' not found for policy '{}'. Creating new one.", attrName, policyName);
                            PolicyAttribute pa = new PolicyAttribute();
                            pa.setPolicy(policy);
                            pa.setAttributeName(attrName);
                            return policyAttributeRepository.save(pa);
                        });

                // Fetch latest AttributeValue
                Optional<AttributeValue> latestAttrValueOpt = attributeValueRepository
                        .findTopByPolicyAttributeOrderByVersionDesc(attribute);

                String oldValue = latestAttrValueOpt.map(AttributeValue::getAttributeValue).orElse(null);
                log.info("Old value: '{}' | New value: '{}'", oldValue, newValue);

                if (!Objects.equals(oldValue, newValue)) {
                    hasAnyChange = true;
                    log.info("Change detected for attribute '{}'.", attrName);
                }

                // Prepare new AttributeValue
                AttributeValue newAttrVal = new AttributeValue();
                newAttrVal.setPolicyAttribute(attribute);
                newAttrVal.setAttributeValue(newValue);
                newAttrVal.setVersion(newVersion);
                newAttributeValues.add(newAttrVal);
            }
        }

        if (hasAnyChange) {
            log.info("Changes detected. Saving new AttributeValues with version {}.", newVersion);
            attributeValueRepository.saveAll(newAttributeValues);
            return ResponseEntity.ok("New version " + newVersion + " created with updated values.");
        } else {
            log.info("No changes detected. Skipping database update.");
            return ResponseEntity.ok("No changes detected. No new version created.");
        }
    }
}