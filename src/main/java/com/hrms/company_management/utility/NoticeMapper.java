package com.hrms.company_management.utility;

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

    public Notice convertToEntity(NoticeRequest noticeRequest) {

        return modelMapper.map(noticeRequest, Notice.class);
    }

    public NoticeResponse convertToResponse(Notice notice) {

        return modelMapper.map(notice, NoticeResponse.class);
    }

    public List<NoticeResponse> convertToResponseList(List<Notice> notices) {
        return notices.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }
}
