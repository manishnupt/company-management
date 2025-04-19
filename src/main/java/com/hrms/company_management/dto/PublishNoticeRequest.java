package com.hrms.company_management.dto;

import lombok.Data;

@Data
public class PublishNoticeRequest {

    private String title;
    private String description;
    private String noticeType;
}
