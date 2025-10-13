package com.hrms.company_management.entity;


import com.hrms.company_management.utility.DisbursalFrequency;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
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
    @Enumerated(EnumType.STRING)
    private DisbursalFrequency disbursalFrequency;

}
