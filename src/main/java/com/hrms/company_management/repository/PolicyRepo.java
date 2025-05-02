package com.hrms.company_management.repository;

import com.hrms.company_management.entity.Policy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PolicyRepo extends JpaRepository<Policy,Long> {

    Optional<Policy> findByPolicyName(String policyName);
}
