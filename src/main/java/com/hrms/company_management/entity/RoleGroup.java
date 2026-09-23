package com.hrms.company_management.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Data
public class RoleGroup {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String description;
    private String kcGroupIdRef;//this represents the keycloak group id

    @ManyToMany
    @JoinTable(
            name = "role_group_roles",
            joinColumns = @JoinColumn(name = "role_group_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<Role> roles = new HashSet<>();

    // policy version this group follows; moved to a newer version once it is published
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "policy_id")
    @JsonIgnore
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Policy policy;

    @Column(updatable = false, name = "creation_date")
    private LocalDateTime creationDate;

    @Column(name = "last_modification_date")
    private LocalDateTime lastModificationDate;

    @PrePersist
    protected void onCreate() {
        creationDate = LocalDateTime.now();
        lastModificationDate = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        lastModificationDate = LocalDateTime.now();
    }// e.g., "Admin Group"


}
