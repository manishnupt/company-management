package com.hrms.company_management.repository;

import java.util.List;
import java.util.UUID;

import com.hrms.company_management.entity.Policy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.hrms.company_management.entity.WFHType;
import com.hrms.company_management.utility.DisbursalFrequency;

@Repository
public interface WFHRepository extends JpaRepository<WFHType, String> {

    @Query("SELECT w FROM WFHType w WHERE w.disbursalFrequency = :frequency")
    List<WFHType> findByDisbursalFrequency(@Param("frequency") DisbursalFrequency frequency);

    List<WFHType> findByPolicy_Id(UUID policyId);

    boolean existsByPolicy_IdAndNameIgnoreCase(UUID policyId, String name);

    boolean existsByPolicy_IdAndNameIgnoreCaseAndIdNot(UUID policyId, String name, String id);
}
