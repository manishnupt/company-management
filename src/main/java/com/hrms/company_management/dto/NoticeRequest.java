package com.hrms.company_management.dto;

import java.time.Instant;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * Body for creating or editing a notice.
 * Date rules that depend on each other or on the current time are checked in NoticeService.
 */
@Data
public class NoticeRequest {

    @NotBlank(message = "title is required")
    @Size(max = 200, message = "title must be at most 200 characters")
    private String title;

    @NotBlank(message = "description is required")
    @Size(max = 5000, message = "description must be at most 5000 characters")
    private String description;

    @NotBlank(message = "noticeType is required")
    @Size(max = 50, message = "noticeType must be at most 50 characters")
    private String noticeType;

    /** Null means "now". A future time schedules the notice. */
    private Instant publishAt;

    /** Null means the notice never expires. Must be after publishAt. */
    private Instant expiresAt;

    /** On create: true keeps the notice as a DRAFT instead of publishing or scheduling it. Ignored on edit. */
    private boolean saveAsDraft;
}
