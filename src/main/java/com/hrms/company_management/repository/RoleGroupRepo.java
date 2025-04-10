package com.hrms.company_management.repository;

import com.hrms.company_management.entity.Role;
import com.hrms.company_management.entity.RoleGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleGroupRepo extends JpaRepository<RoleGroup ,Long> {

    Optional<RoleGroup> findByName(String name);

}
