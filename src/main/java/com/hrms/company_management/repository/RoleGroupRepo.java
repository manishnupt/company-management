package com.hrms.company_management.repository;

import com.hrms.company_management.entity.Role;
import com.hrms.company_management.entity.RoleGroup;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RoleGroupRepo extends JpaRepository<RoleGroup ,Long> {

    Optional<RoleGroup> findByName(String name);

    // Loads roles in the same query so every group can be cached with its roles
    @EntityGraph(attributePaths = "roles")
    @Query("select g from RoleGroup g")
    List<RoleGroup> findAllWithRoles();

}
