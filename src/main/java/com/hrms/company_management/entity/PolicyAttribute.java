package com.hrms.company_management.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "policy_attributes")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PolicyAttribute {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long attributeId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "policy_id", nullable = false)
    private Policy policy;

    @Column(nullable = false)
    private String attributeName; // e.g., "mandatoryOfficeDays", "checkInTime"

    @OneToMany(mappedBy = "policyAttribute", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AttributeValue> values = new ArrayList<>();
}
