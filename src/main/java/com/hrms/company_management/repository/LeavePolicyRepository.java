package com.hrms.company_management.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.hrms.company_management.entity.LeaveType;
import com.hrms.company_management.utility.DisbursalFrequency;

public interface LeavePolicyRepository extends JpaRepository<LeaveType, String> {

    @Query("SELECT l FROM LeaveType l WHERE l.disbursalFrequency = :frequency")
    List<LeaveType> findByDisbursalFrequency(@Param("frequency") DisbursalFrequency frequency);

}
