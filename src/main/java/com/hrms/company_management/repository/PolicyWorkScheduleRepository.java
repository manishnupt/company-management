package com.hrms.company_management.repository;

import com.hrms.company_management.entity.PolicyWorkSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface PolicyWorkScheduleRepository extends JpaRepository<PolicyWorkSchedule, UUID> {

    Optional<PolicyWorkSchedule> findByPolicyId(UUID policyId);
}
