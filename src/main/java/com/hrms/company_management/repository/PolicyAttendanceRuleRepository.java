package com.hrms.company_management.repository;

import com.hrms.company_management.entity.PolicyAttendanceRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface PolicyAttendanceRuleRepository extends JpaRepository<PolicyAttendanceRule, UUID> {

    Optional<PolicyAttendanceRule> findByPolicyId(UUID policyId);
}
