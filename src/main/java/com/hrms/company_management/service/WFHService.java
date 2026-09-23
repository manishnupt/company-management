package com.hrms.company_management.service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.hrms.company_management.dto.CreateWfhTypeRequest;
import com.hrms.company_management.dto.WfhTypeResponse;
import com.hrms.company_management.entity.Policy;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.hrms.company_management.dto.WFHRequest;
import com.hrms.company_management.dto.WFHResponse;
import com.hrms.company_management.entity.WFHType;
import com.hrms.company_management.repository.WFHRepository;
import com.hrms.company_management.utility.DisbursalFrequency;
import com.hrms.company_management.utility.WFHMapper;

@Service
@Log4j2
public class WFHService {

    @Autowired
    private WFHRepository repository;

    @Autowired
    private WFHMapper wfhMapper;

    @Autowired
    private PolicyService policyService;

    // Add service methods here
    // create
    public WFHResponse createWFHType(WFHRequest wfhRequest) {
        log.info("Creating WFH type: {}", wfhRequest);
        WFHType wfhType = wfhMapper.convertToEntity(wfhRequest);
        wfhType.setId(UUID.randomUUID().toString());
        WFHType saved = repository.save(wfhType);
        return wfhMapper.convertToResponse(saved);
    }

    // get all
    public List<WFHResponse> getAllWFHTypes() {
        log.info("Fetching all WFH types");
        return wfhMapper.convertToResponseList(repository.findAll());
    }

    // read
    public WFHResponse getWFHTypeById(String id) {
        log.info("Fetching WFH type by id: {}", id);
        return repository.findById(id)
                .map(wfhMapper::convertToResponse)
                .orElse(null);
    }

    // update
    public WFHResponse updateWFHType(String id, WFHRequest updatedWFHType) {
        log.info("Updating WFH type with id: {}", id);
        WFHType existing = repository.findById(id).orElse(null);
        if (existing == null) {
            log.info("WFH type not found with id: {}", id);
            return null;
        }

        existing.setName(updatedWFHType.getName());
        existing.setTotalDays(updatedWFHType.getTotalDays());
        existing.setDisbursalFrequency(updatedWFHType.getDisbursalFrequency());
        existing.setDescription(updatedWFHType.getDescription());

        return wfhMapper.convertToResponse(repository.save(existing));
    }

    // delete
    public boolean deleteWFHType(String id) {
        log.info("Deleting WFH type with id: {}", id);
        if (!repository.existsById(id))
            return false;
        repository.deleteById(id);
        return true;
    }

    public List<WFHResponse> getByType(String type) {
        log.info("Fetching WFH types by disbursal frequency: {}", type);
        DisbursalFrequency disbursalFrequency = DisbursalFrequency.valueOf(type.toUpperCase());
        return wfhMapper.convertToResponseList(repository.findByDisbursalFrequency(disbursalFrequency));
    }

    public WfhTypeResponse create(CreateWfhTypeRequest request, String policyId) {
        Policy policyById = policyService.getPolicyEntityById(UUID.fromString(policyId));

        log.info("Creating WFH type: {}", request);
        WFHType wfhType = new WFHType();
        wfhType.setId(UUID.randomUUID().toString());
        wfhType.setPolicy(policyById);
        wfhType.setName(request.getName());
        wfhType.setTotalDays(request.getTotalDays());
        wfhType.setDisbursalFrequency(request.getDisbursalFrequency());
        WFHType saved = repository.save(wfhType);
        return wfhMapper.convertToWfhTypeResponse(saved);
    }

    public List<WfhTypeResponse> getWfhTypeByPolicyId(String policyId) {
        log.info("Fetching WFH types by policy id: {}", policyId);
        Policy policyById = policyService.getPolicyEntityById(UUID.fromString(policyId));
        List<WFHType> wfhTypes = repository.findByPolicyId(policyById);
        List<WfhTypeResponse> wfhTypeResponses = new ArrayList<>();
        for (WFHType wfhType : wfhTypes) {
            WfhTypeResponse response = wfhMapper.convertToWfhTypeResponse(wfhType);
            wfhTypeResponses.add(response);
        }
        return wfhTypeResponses;
    }
}
