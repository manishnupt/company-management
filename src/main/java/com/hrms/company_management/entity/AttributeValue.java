package com.hrms.company_management.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "attribute_values")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AttributeValue {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long valueId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "attribute_id", nullable = false)
    private PolicyAttribute policyAttribute;

    @Column(nullable = false)
    private String attributeValue; // e.g., "Monday,Tuesday", "09:00"

    private int version;
}

