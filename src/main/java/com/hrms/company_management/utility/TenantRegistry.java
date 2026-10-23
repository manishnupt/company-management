package com.hrms.company_management.utility;

import java.util.Set;
import java.util.stream.Collectors;

import javax.sql.DataSource;

import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;
import org.springframework.stereotype.Component;

/**
 * Knows which tenants have a configured database.
 * Needed because the routing DataSource silently falls back to the default tenant for an unknown key,
 * and because background jobs have no X-Tenant-Id header and must visit every tenant themselves.
 */
@Component
public class TenantRegistry {

    private final DataSource dataSource;

    public TenantRegistry(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    /** False when running against a single plain DataSource (for example in tests). */
    public boolean isRoutingEnabled() {
        return dataSource instanceof AbstractRoutingDataSource;
    }

    public Set<String> tenantIds() {
        if (dataSource instanceof AbstractRoutingDataSource routing) {
            return routing.getResolvedDataSources().keySet().stream()
                    .map(String::valueOf)
                    .collect(Collectors.toUnmodifiableSet());
        }
        return Set.of();
    }

    public boolean isKnownTenant(String tenantId) {
        return !isRoutingEnabled() || tenantIds().contains(tenantId);
    }
}
