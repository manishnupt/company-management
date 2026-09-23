package com.hrms.company_management.entity;


import com.hrms.company_management.utility.DisbursalFrequency;

import jakarta.persistence.*;
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
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "policy_id", nullable = false)
    private Policy policy;

}
