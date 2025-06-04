package com.hrms.company_management.repository;

import com.hrms.company_management.entity.LeaveType;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LeavePolicyRepository extends JpaRepository<LeaveType, String> {
}
