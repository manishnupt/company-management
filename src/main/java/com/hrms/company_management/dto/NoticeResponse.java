package com.hrms.company_management.dto;

import java.time.LocalDate;

import lombok.Data;

@Data
public class NoticeResponse {

    private Long id;
    private String title;
    private String description;
    private String noticeType;
    private LocalDate createdAt;
    private LocalDate updatedAt;
    
}
