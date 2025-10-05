package com.hrms.company_management.entity;

import org.springframework.data.annotation.Id;

import com.hrms.company_management.utility.DisbursalFrequency;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "wfh_types")
public class WFHType {

    @Id
    private String id;
    private String name;
    private String description;
    private int totalDays;
    private DisbursalFrequency disbursalFrequency;

}
