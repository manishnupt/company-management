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
        log.info("createWFHType called with wfhType:{}", wfhType);
        wfhType.setId(UUID.randomUUID().toString());
        return repository.save(wfhType);
    }

    // get all
    public List<WFHType> getAllWFHTypes() {
        log.info("getAllWFHTypes called");
        return repository.findAll();
    }

    // read
    public WFHType getWFHTypeById(String id) {
        log.info("getWFHTypeById called with id:{}", id);
        return repository.findById(id).orElse(null);
    }

    // update
    public WFHType updateWFHType(String id, WFHType updatedWFHType) {
        log.info("updateWFHType called with id:{}, updatedWFHType:{}", id, updatedWFHType);
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
        log.info("deleteWFHType called with id:{}", id);
        if (!repository.existsById(id))
            return false;
        repository.deleteById(id);
        return true;
    }

    public List<WFHType> getByType(String type) {
         log.info("getByType called with type:{}", type);
         DisbursalFrequency disbursalFrequency = DisbursalFrequency.valueOf(type.toUpperCase());
            return repository.findByDisbursalFrequency(disbursalFrequency);

    }

}
