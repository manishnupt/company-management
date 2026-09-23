package com.hrms.company_management.service;

import com.hrms.company_management.dto.CreateLeaveTypeRequest;
import com.hrms.company_management.dto.LeaveTypeResponse;
import com.hrms.company_management.dto.PolicyResponse;
import com.hrms.company_management.entity.LeaveType;
import com.hrms.company_management.entity.Policy;
import com.hrms.company_management.repository.LeavePolicyRepository;
import com.hrms.company_management.utility.DisbursalFrequency;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@Log4j2
public class LeavePolicyService {
    @Autowired
    private LeavePolicyRepository repository;

    @Autowired
    private PolicyService policyService;

    public List<LeaveType> getAll() {
        log.info("Fetching all leave types");
        return repository.findAll();
    }

    public LeaveType getById(String id) {
        log.info("Fetching leave type by id: {}", id);
        return repository.findById(id).orElse(null);
    }

    public List<LeaveType> getByType(String type) {
        log.info("Fetching leave types by disbursal frequency: {}", type);
        DisbursalFrequency disbursalFrequency = DisbursalFrequency.valueOf(type.toUpperCase());
            return repository.findByDisbursalFrequency(disbursalFrequency);
    }

    public List<LeaveTypeResponse> getByPolicyId(String policyId) {
        log.info("Fetching leave types by policy id: {}", policyId);
        return repository.findByPolicy_Id(UUID.fromString(policyId)).stream()
                .map(this::convertToResponseDto)
                .toList();
    }

    public LeaveTypeResponse create(CreateLeaveTypeRequest leaveType, String policyId) {
        log.info("Creating leave type: {}", leaveType);
        Policy policyById = policyService.getPolicyEntityById(UUID.fromString(policyId));
        LeaveType save = repository.save(convertToEntity(leaveType,policyById));
        return convertToResponseDto(save);
    }

    private LeaveTypeResponse convertToResponseDto(LeaveType save) {
        return LeaveTypeResponse.builder()
                .id(save.getId())
                .policyId(save.getPolicy().getId())
                .name(save.getName())
                .totalDays(save.getTotalDays())
                .carryForward(save.isCarryForward())
                .disbursalFrequency(save.getDisbursalFrequency())
                .description(save.getDescription())
                .build();
    }

    private LeaveType convertToEntity(CreateLeaveTypeRequest leaveTypeRequest, Policy policyId) {
        LeaveType leaveType = new LeaveType();
        leaveType.setId(UUID.randomUUID().toString());
        leaveType.setName(leaveTypeRequest.getName());
        leaveType.setTotalDays(leaveTypeRequest.getTotalDays());
        leaveType.setCarryForward(leaveTypeRequest.isCarryForward());
        leaveType.setDisbursalFrequency(leaveTypeRequest.getDisbursalFrequency());
        leaveType.setDescription(leaveTypeRequest.getDescription());
        leaveType.setPolicy(policyId);
        return leaveType;

    }

    public LeaveType update(String id, LeaveType updatedLeaveType) {
        log.info("Updating leave type with id: {}", id);
        LeaveType existing = repository.findById(id).orElse(null);
        if (existing == null) {
            log.info("Leave type not found with id: {}", id);
            return null;
        }

        existing.setName(updatedLeaveType.getName());
        existing.setTotalDays(updatedLeaveType.getTotalDays());
        existing.setCarryForward(updatedLeaveType.isCarryForward());
        existing.setDisbursalFrequency(updatedLeaveType.getDisbursalFrequency());
        existing.setDescription(updatedLeaveType.getDescription());

        return repository.save(existing);
    }

    public boolean delete(String id) {
        log.info("Deleting leave type with id: {}", id);
        if (!repository.existsById(id)) return false;
        repository.deleteById(id);
        return true;
    }
}
