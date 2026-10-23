package com.hrms.company_management.dto;

import java.time.Instant;
import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.hrms.company_management.entity.NoticeStatus;

import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class NoticeResponse {

    private Long id;
    private String title;
    private String description;
    private String noticeType;

    /** Effective status: accounts for publish/expiry times even if the scheduler has not run yet. */
    private NoticeStatus status;
    private Instant publishAt;
    private Instant expiresAt;

    private LocalDate createdAt;
    private LocalDate updatedAt;
}
