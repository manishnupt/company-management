package com.hrms.company_management.service;

import com.hrms.company_management.entity.LeaveType;
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

    public LeaveType create(LeaveType leaveType) {
        log.info("Creating leave type: {}", leaveType);
        leaveType.setId(UUID.randomUUID().toString());
        return repository.save(leaveType);
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
