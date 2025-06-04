package com.hrms.company_management.service;

import com.hrms.company_management.dto.LeaveAssignmentDto;
import com.hrms.company_management.dto.BulkLeaveAssignmentDto;
import com.hrms.company_management.dto.LeaveTypeDto;
import com.hrms.company_management.entity.LeaveType;
import com.hrms.company_management.repository.LeavePolicyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class LeavePolicyService {
    @Autowired
    private LeavePolicyRepository repository;

    @Autowired
    private RestTemplate restTemplate;

    @Value("${employee.service.base.url}")
    private String employeeServiceBaseUrl;

    public List<LeaveType> getAll() {
        return repository.findAll();
    }

    public LeaveType getById(String id) {
        return repository.findById(id).orElse(null);
    }

    public LeaveType getByName(String name){
        return repository.findByNameAndIsActiveTrue(name)
                .orElse(null);
    }

    public LeaveType create(LeaveTypeDto leaveTypeDto) {

        LeaveType leaveType = mapToEntity(leaveTypeDto);
        leaveType.setId(UUID.randomUUID().toString());

        LeaveType savedLeaveType = repository.save(leaveType);

        notifyEmployeeServiceForNewLeaveType(savedLeaveType);

        return savedLeaveType;
    }

    public LeaveType update(String id, LeaveTypeDto leaveTypeDto) {

        LeaveType existing = repository.findById(id).orElse(null);
        if (existing == null) return null;

        updateEntityFromDto(existing, leaveTypeDto);

        return repository.save(existing);
    }

    public boolean delete(String id) {
        if (!repository.existsById(id)) return false;

        LeaveType leaveType = getById(id);
        leaveType.setActive(false);
        repository.save(leaveType);

        notifyEmployeeServiceForLeaveTypeDeactivation(id);

        return true;
    }

    public ResponseEntity<String> assignLeaveToAllEmployees(BulkLeaveAssignmentDto assignmentDto) {
        LeaveType leaveType = getById(assignmentDto.getLeaveTypeId());

        String url = employeeServiceBaseUrl + "/employee/leave-balance/bulk-assign";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<BulkLeaveAssignmentDto> entity = new HttpEntity<>(assignmentDto, headers);

        try {
            return restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
        } catch (Exception e) {
            throw new RuntimeException("Failed to assign leave to employees: " + e.getMessage());
        }
    }

    public ResponseEntity<String> assignLeaveToEmployee(String employeeId, String leaveTypeId, int days) {
        LeaveType leaveType = getById(leaveTypeId);

        String url = employeeServiceBaseUrl + "/employee/leave-balance/assign/" + employeeId;

        LeaveAssignmentDto assignmentDto = new LeaveAssignmentDto();
        assignmentDto.setLeaveTypeId(leaveTypeId);
        assignmentDto.setDays(days);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<LeaveAssignmentDto> entity = new HttpEntity<>(assignmentDto, headers);

        try {
            return restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
        } catch (Exception e) {
            throw new RuntimeException("Failed to assign leave to employee: " + e.getMessage());
        }
    }

    private void notifyEmployeeServiceForNewLeaveType(LeaveType leaveType) {
        String url = employeeServiceBaseUrl + "/employee/leave-balance/initialize-for-new-leave-type";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<LeaveType> entity = new HttpEntity<>(leaveType, headers);

        try {
            restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
        } catch (Exception e) {
            System.err.println("Failed to notify employee service for new leave type: " + e.getMessage());
        }
    }

    private void notifyEmployeeServiceForLeaveTypeDeactivation(String leaveTypeId) {
        String url = employeeServiceBaseUrl + "/employee/leave-balance/deactivate-leave-type/" + leaveTypeId;

        try {
            restTemplate.exchange(url, HttpMethod.PUT, null, String.class);
        } catch (Exception e) {
            System.err.println("Failed to notify employee service for leave type deactivation: " + e.getMessage());
        }
    }

    private LeaveType mapToEntity(LeaveTypeDto dto) {
        LeaveType leaveType = new LeaveType();
        leaveType.setName(dto.getName());
        leaveType.setDefaultTotalDays(dto.getDefaultTotalDays());
        leaveType.setCarryForward(dto.isCarryForward());
        leaveType.setMaxCarryForwardDays(dto.getMaxCarryForwardDays());
        leaveType.setDisbursalFrequency(dto.getDisbursalFrequency());
        leaveType.setDescription(dto.getDescription());
        leaveType.setActive(true);
        return leaveType;
    }

    private void updateEntityFromDto(LeaveType existing, LeaveTypeDto dto) {
        existing.setName(dto.getName());
        existing.setDefaultTotalDays(dto.getDefaultTotalDays());
        existing.setCarryForward(dto.isCarryForward());
        existing.setMaxCarryForwardDays(dto.getMaxCarryForwardDays());
        existing.setDisbursalFrequency(dto.getDisbursalFrequency());
        existing.setDescription(dto.getDescription());
    }
}
