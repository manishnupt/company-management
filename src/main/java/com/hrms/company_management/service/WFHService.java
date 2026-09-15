package com.hrms.company_management.service;

import java.util.List;
import java.util.UUID;

import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.hrms.company_management.entity.WFHType;
import com.hrms.company_management.repository.WFHRepository;
import com.hrms.company_management.utility.DisbursalFrequency;

@Service
@Log4j2
public class WFHService {

    @Autowired
    private WFHRepository repository;

    // Add service methods here
    // create
    public WFHType createWFHType(WFHType wfhType) {
        log.info("Creating WFH type: {}", wfhType);
        wfhType.setId(UUID.randomUUID().toString());
        return repository.save(wfhType);
    }

    // get all
    public List<WFHType> getAllWFHTypes() {
        log.info("Fetching all WFH types");
        return repository.findAll();
    }

    // read
    public WFHType getWFHTypeById(String id) {
        log.info("Fetching WFH type by id: {}", id);
        return repository.findById(id).orElse(null);
    }

    // update
    public WFHType updateWFHType(String id, WFHType updatedWFHType) {
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

        return repository.save(existing);
    }

    // delete
    public boolean deleteWFHType(String id) {
        log.info("Deleting WFH type with id: {}", id);
        if (!repository.existsById(id))
            return false;
        repository.deleteById(id);
        return true;
    }

    public List<WFHType> getByType(String type) {
        log.info("Fetching WFH types by disbursal frequency: {}", type);
        DisbursalFrequency disbursalFrequency = DisbursalFrequency.valueOf(type.toUpperCase());
            return repository.findByDisbursalFrequency(disbursalFrequency);

    }

}
