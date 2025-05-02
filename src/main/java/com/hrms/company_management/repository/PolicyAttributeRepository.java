package com.hrms.company_management.repository;

import com.hrms.company_management.entity.Policy;
import com.hrms.company_management.entity.PolicyAttribute;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PolicyAttributeRepository extends JpaRepository<PolicyAttribute,Long> {

    Optional<PolicyAttribute> findByPolicyAndAttributeName(Policy policy, String attrName);
}
