package com.hrms.company_management.service;

import com.hrms.company_management.entity.LeaveType;
import com.hrms.company_management.repository.LeavePolicyRepository;
import com.hrms.company_management.utility.DisbursalFrequency;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class LeavePolicyService {
    @Autowired
    private LeavePolicyRepository repository;

    public List<LeaveType> getAll() {
        return repository.findAll();
    }

    public LeaveType getById(String id) {
        return repository.findById(id).orElse(null);
    }

    public List<LeaveType> getByType(String type) {
        DisbursalFrequency disbursalFrequency = DisbursalFrequency.valueOf(type.toUpperCase());
            return repository.findByDisbursalFrequency(disbursalFrequency);
    }

    public LeaveType create(LeaveType leaveType) {
        leaveType.setId(UUID.randomUUID().toString());
        return repository.save(leaveType);
    }

    public LeaveType update(String id, LeaveType updatedLeaveType) {
        LeaveType existing = repository.findById(id).orElse(null);
        if (existing == null) return null;

        existing.setName(updatedLeaveType.getName());
        existing.setTotalDays(updatedLeaveType.getTotalDays());
        existing.setCarryForward(updatedLeaveType.isCarryForward());
        existing.setDisbursalFrequency(updatedLeaveType.getDisbursalFrequency());
        existing.setDescription(updatedLeaveType.getDescription());

        return repository.save(existing);
    }

    public boolean delete(String id) {
        if (!repository.existsById(id)) return false;
        repository.deleteById(id);
        return true;
    }
}
