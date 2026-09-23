package com.hrms.company_management.service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.hrms.company_management.dto.CreateWfhTypeRequest;
import com.hrms.company_management.dto.WfhTypeResponse;
import com.hrms.company_management.entity.Policy;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

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
        policyService.requireDraft(existing.getPolicy());
        if (updatedWFHType.getName() == null || updatedWFHType.getName().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "WFH type name is required");
        }
        String name = updatedWFHType.getName().trim();
        requireUniqueName(existing.getPolicy().getId(), name, id);

        existing.setName(name);
        existing.setTotalDays(updatedWFHType.getTotalDays());
        existing.setDisbursalFrequency(updatedWFHType.getDisbursalFrequency());
        existing.setDescription(updatedWFHType.getDescription());

        return wfhMapper.convertToResponse(repository.save(existing));
    }

    // delete
    public boolean deleteWFHType(String id) {
        log.info("Deleting WFH type with id: {}", id);
        WFHType existing = repository.findById(id).orElse(null);
        if (existing == null)
            return false;
        policyService.requireDraft(existing.getPolicy());
        repository.delete(existing);
        return true;
    }

    // WFH type names must be unique within a policy; excludeId skips the WFH type being updated
    private void requireUniqueName(UUID policyId, String name, String excludeId) {
        boolean exists = excludeId == null
                ? repository.existsByPolicy_IdAndNameIgnoreCase(policyId, name)
                : repository.existsByPolicy_IdAndNameIgnoreCaseAndIdNot(policyId, name, excludeId);
        if (exists) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "WFH type '" + name + "' already exists in policy " + policyId);
        }
    }

    public List<WFHResponse> getByType(String type) {
        log.info("Fetching WFH types by disbursal frequency: {}", type);
        DisbursalFrequency disbursalFrequency = DisbursalFrequency.valueOf(type.toUpperCase());
        return wfhMapper.convertToResponseList(repository.findByDisbursalFrequency(disbursalFrequency));
    }

    public WfhTypeResponse create(CreateWfhTypeRequest request, String policyId) {
        Policy policyById = policyService.getDraftPolicyEntityById(UUID.fromString(policyId));
        String name = request.getName().trim();
        requireUniqueName(policyById.getId(), name, null);

        log.info("Creating WFH type: {}", request);
        WFHType wfhType = new WFHType();
        wfhType.setId(UUID.randomUUID().toString());
        wfhType.setPolicy(policyById);
        wfhType.setName(name);
        wfhType.setDescription(request.getDescription());
        wfhType.setTotalDays(request.getTotalDays());
        wfhType.setDisbursalFrequency(request.getDisbursalFrequency());
        WFHType saved = repository.save(wfhType);
        return wfhMapper.convertToWfhTypeResponse(saved);
    }

    public List<WfhTypeResponse> getWfhTypeByPolicyId(String policyId) {
        log.info("Fetching WFH types by policy id: {}", policyId);
        policyService.getPolicyEntityById(UUID.fromString(policyId));
        List<WFHType> wfhTypes = repository.findByPolicy_Id(UUID.fromString(policyId));
        List<WfhTypeResponse> wfhTypeResponses = new ArrayList<>();
        for (WFHType wfhType : wfhTypes) {
            WfhTypeResponse response = wfhMapper.convertToWfhTypeResponse(wfhType);
            wfhTypeResponses.add(response);
        }
        return wfhTypeResponses;
    }
}
