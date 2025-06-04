package com.hrms.company_management.repository;

import com.hrms.company_management.entity.LeaveType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface LeavePolicyRepository extends JpaRepository<LeaveType, String> {

    List<LeaveType> findByIsActiveTrue();
    Optional<LeaveType> findByIdAndIsActiveTrue(String id);
    Optional<LeaveType> findByNameAndIsActiveTrue(String name);
    boolean existsByNameAndIsActiveTrue(String name);
}
