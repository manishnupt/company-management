package com.hrms.company_management.repository;

import java.util.List;

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


}
