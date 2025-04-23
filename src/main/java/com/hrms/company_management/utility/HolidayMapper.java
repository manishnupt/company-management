package com.hrms.company_management.utility;

import java.util.List;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

import com.hrms.company_management.dto.HolidayRequest;
import com.hrms.company_management.dto.HolidayResponse;
import com.hrms.company_management.entity.Holiday;

@Component
public class HolidayMapper {

    private final ModelMapper modelMapper;

    public HolidayMapper(ModelMapper modelMapper) {
        this.modelMapper = modelMapper;

    }

    public Holiday convertToEntity(HolidayRequest holidayRequest) {

        return modelMapper.map(holidayRequest, Holiday.class);
    }

    public HolidayResponse convertToResponse(Holiday holiday) {

        return modelMapper.map(holiday, HolidayResponse.class);
    }

    public List<HolidayResponse> convertToResponseList(List<Holiday> holidays) {
        return holidays.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }
}
