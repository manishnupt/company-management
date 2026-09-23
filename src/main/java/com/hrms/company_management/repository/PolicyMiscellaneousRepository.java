package com.hrms.company_management.repository;

import com.hrms.company_management.entity.PolicyMiscellaneous;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface PolicyMiscellaneousRepository extends JpaRepository<PolicyMiscellaneous, UUID> {

    Optional<PolicyMiscellaneous> findByPolicyId(UUID policyId);
}
