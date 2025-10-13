package com.hrms.company_management.service;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.hrms.company_management.entity.WFHType;
import com.hrms.company_management.repository.WFHRepository;
import com.hrms.company_management.utility.DisbursalFrequency;

@Service
public class WFHService {

    @Autowired
    private WFHRepository repository;

    // Add service methods here
    // create
    public WFHType createWFHType(WFHType wfhType) {
        wfhType.setId(UUID.randomUUID().toString());
        return repository.save(wfhType);
    }

    // get all
    public List<WFHType> getAllWFHTypes() {
        return repository.findAll();
    }

    // read
    public WFHType getWFHTypeById(String id) {
        System.out.println(id);
        return repository.findById(id).orElse(null);
    }

    // update
    public WFHType updateWFHType(String id, WFHType updatedWFHType) {
        WFHType existing = repository.findById(id).orElse(null);
        if (existing == null)
            return null;

        existing.setName(updatedWFHType.getName());
        existing.setTotalDays(updatedWFHType.getTotalDays());
        existing.setDisbursalFrequency(updatedWFHType.getDisbursalFrequency());
        existing.setDescription(updatedWFHType.getDescription());

        return repository.save(existing);
    }

    // delete
    public boolean deleteWFHType(String id) {
        if (!repository.existsById(id))
            return false;
        repository.deleteById(id);
        return true;
    }

    public List<WFHType> getByType(String type) {
         DisbursalFrequency disbursalFrequency = DisbursalFrequency.valueOf(type.toUpperCase());
            return repository.findByDisbursalFrequency(disbursalFrequency);
        
    }

}
