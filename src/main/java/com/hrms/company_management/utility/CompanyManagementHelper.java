package com.hrms.company_management.utility;

import com.hrms.company_management.dto.GenerateTokenRequest;

import java.util.Map;
public class CompanyManagementHelper {
    public static GenerateTokenRequest getGenerateTokenRequest(Map<String, Object> configMap) {
        String clientId = (String) configMap.get("clientId");
        String clientSecret = (String) configMap.get("clientSecret");
        String username = (String) configMap.get("username");
        String password = (String) configMap.get("password");
        return new GenerateTokenRequest(password, username, clientId, clientSecret);
    }
}
