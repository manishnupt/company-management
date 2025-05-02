package com.hrms.company_management.repository;

import com.hrms.company_management.entity.AttributeValue;
import com.hrms.company_management.entity.PolicyAttribute;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AttributeValueRepository extends JpaRepository<AttributeValue,Long> {
    @Query("SELECT MAX(av.version) FROM AttributeValue av")
    Optional<Integer> findMaxVersion();

    Optional<AttributeValue> findTopByPolicyAttributeOrderByVersionDesc(PolicyAttribute attribute);
}
