package com.hrms.company_management.utility;

import java.util.List;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

import com.hrms.company_management.dto.WFHRequest;
import com.hrms.company_management.dto.WFHResponse;
import com.hrms.company_management.entity.WFHType;

@Component
public class WFHMapper {

    private final ModelMapper modelMapper;

    public WFHMapper(ModelMapper modelMapper) {
        this.modelMapper = modelMapper;
    }

    public WFHType convertToEntity(WFHRequest wfhRequest) {
        return modelMapper.map(wfhRequest, WFHType.class);
    }

    public WFHResponse convertToResponse(WFHType wfhType) {
        return modelMapper.map(wfhType, WFHResponse.class);
    }

    public List<WFHResponse> convertToResponseList(List<WFHType> wfhTypes) {
        return wfhTypes.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }
}
