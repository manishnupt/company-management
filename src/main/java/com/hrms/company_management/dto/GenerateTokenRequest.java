package com.hrms.company_management.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GenerateTokenRequest {
    private String password;
    private String username;
    private String clientId;
    private String clientSecret;
}
