package com.hrms.company_management.repository;

import com.hrms.company_management.entity.Policy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface PoliciesRepository extends JpaRepository<Policy, UUID> {
}
