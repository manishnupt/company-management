package com.hrms.company_management.dto;

import java.util.Set;

/** The authenticated caller of a notice endpoint. */
public record NoticeCaller(String userId, String tenantId, Set<String> roles) {
}
