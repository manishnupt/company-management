package com.hrms.company_management.repository;

import com.hrms.company_management.entity.Policy;
import com.hrms.company_management.utility.PolicyStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface PoliciesRepository extends JpaRepository<Policy, UUID> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM Policy p WHERE p.id = :id")
    Optional<Policy> findByIdForUpdate(@Param("id") UUID id);

    @Query("SELECT MAX(p.version) FROM Policy p WHERE p.rootPolicyId = :rootPolicyId")
    Optional<Integer> findMaxVersionByRootPolicyId(@Param("rootPolicyId") UUID rootPolicyId);

    boolean existsByRootPolicyIdAndStatus(UUID rootPolicyId, PolicyStatus status);

    boolean existsByNameIgnoreCaseAndVersion(String name, Integer version);

    boolean existsByNameIgnoreCaseAndVersionAndIdNot(String name, Integer version, UUID id);
}
