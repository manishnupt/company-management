package com.hrms.company_management.utility;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

import com.hrms.company_management.dto.NoticeRequest;
import com.hrms.company_management.dto.NoticeResponse;
import com.hrms.company_management.entity.Notice;

@Component
public class NoticeMapper {

    private final ModelMapper modelMapper;

    public NoticeMapper(ModelMapper modelMapper) {
        this.modelMapper = modelMapper;
    }

    /** Copies the editable fields from the request. Lifecycle fields (status, audit) are set by the service. */
    public void applyRequest(NoticeRequest request, Notice notice) {
        notice.setTitle(request.getTitle().trim());
        notice.setDescription(request.getDescription().trim());
        notice.setNoticeType(request.getNoticeType().trim());
        notice.setPublishAt(request.getPublishAt());
        notice.setExpiresAt(request.getExpiresAt());
    }

    public NoticeResponse convertToResponse(Notice notice, Instant now) {
        NoticeResponse response = modelMapper.map(notice, NoticeResponse.class);
        response.setStatus(NoticeLifecycle.effectiveStatus(notice, now));
        return response;
    }

    public List<NoticeResponse> convertToResponseList(List<Notice> notices, Instant now) {
        return notices.stream()
                .map(notice -> convertToResponse(notice, now))
                .collect(Collectors.toList());
    }
}
