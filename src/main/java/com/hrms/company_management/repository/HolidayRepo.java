package com.hrms.company_management.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.hrms.company_management.entity.Holiday;

@Repository
public interface HolidayRepo extends JpaRepository<Holiday, Long> {

}
