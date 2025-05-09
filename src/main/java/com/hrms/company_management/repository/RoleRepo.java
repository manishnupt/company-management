package com.hrms.company_management.repository;

import com.hrms.company_management.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RoleRepo extends JpaRepository<Role,Long> {

    Optional<Role> findByName(String name);

    List<Role> findByModuleIn(List<String> modules);
}
