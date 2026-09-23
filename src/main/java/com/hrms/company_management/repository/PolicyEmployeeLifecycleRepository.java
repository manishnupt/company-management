package com.hrms.company_management.repository;

import com.hrms.company_management.entity.PolicyEmployeeLifecycle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface PolicyEmployeeLifecycleRepository extends JpaRepository<PolicyEmployeeLifecycle, UUID> {

    Optional<PolicyEmployeeLifecycle> findByPolicyId(UUID policyId);
}
