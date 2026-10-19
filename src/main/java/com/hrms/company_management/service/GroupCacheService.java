package com.hrms.company_management.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hrms.company_management.dto.GroupResponse;
import com.hrms.company_management.entity.RoleGroup;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * Tenant-scoped Redis cache for role groups.
 * Every Redis failure is logged and falls back to the database, so Redis is never required for correctness.
 */
@Service
@Log4j2
public class GroupCacheService {

    private static final String KEY_PREFIX = "company-mgmt:groups:";
    private static final TypeReference<List<GroupResponse>> GROUP_LIST_TYPE = new TypeReference<>() {};

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;
    private final boolean enabled;
    private final Duration ttl;

    public GroupCacheService(StringRedisTemplate redisTemplate,
                             ObjectMapper objectMapper,
                             @Value("${groups.cache.enabled:true}") boolean enabled,
                             @Value("${groups.cache.ttl-hours:12}") long ttlHours) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
        this.enabled = enabled;
        this.ttl = Duration.ofHours(ttlHours);
    }

    public List<GroupResponse> getAllGroups(String tenant, Supplier<List<GroupResponse>> loader) {
        return getOrLoad(tenant, allGroupsKey(tenant), GROUP_LIST_TYPE, loader);
    }

    public RoleGroup getGroupById(String tenant, Long groupId, Supplier<RoleGroup> loader) {
        return getOrLoad(tenant, groupKey(tenant, groupId), new TypeReference<RoleGroup>() {}, loader);
    }

    /** Clears the "all groups" list and, if given, the single group entry. Runs after commit when inside a transaction. */
    public void evict(String tenant, Long groupId) {
        if (!isUsable(tenant)) {
            return;
        }
        Runnable eviction = () -> {
            try {
                if (groupId != null) {
                    redisTemplate.delete(List.of(allGroupsKey(tenant), groupKey(tenant, groupId)));
                } else {
                    redisTemplate.delete(allGroupsKey(tenant));
                }
                log.info("Evicted group cache for tenant:{} groupId:{}", tenant, groupId);
            } catch (Exception e) {
                log.warn("Failed to evict group cache for tenant:{} groupId:{}: {}", tenant, groupId, e.getMessage());
            }
        };

        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    eviction.run();
                }
            });
        } else {
            eviction.run();
        }
    }

    private <T> T getOrLoad(String tenant, String key, TypeReference<T> type, Supplier<T> loader) {
        if (!isUsable(tenant)) {
            return loader.get();
        }

        Optional<T> cached = read(key, type);
        if (cached.isPresent()) {
            log.info("Group cache hit key:{}", key);
            return cached.get();
        }

        log.info("Group cache miss key:{}", key);
        T value = loader.get();
        write(key, value);
        return value;
    }

    private <T> Optional<T> read(String key, TypeReference<T> type) {
        try {
            String json = redisTemplate.opsForValue().get(key);
            return json == null ? Optional.empty() : Optional.ofNullable(objectMapper.readValue(json, type));
        } catch (Exception e) {
            log.warn("Group cache read failed for key:{}, falling back to DB: {}", key, e.getMessage());
            return Optional.empty();
        }
    }

    private void write(String key, Object value) {
        if (value == null) {
            return;
        }
        try {
            redisTemplate.opsForValue().set(key, objectMapper.writeValueAsString(value), ttl);
        } catch (Exception e) {
            log.warn("Group cache write failed for key:{}: {}", key, e.getMessage());
        }
    }

    private boolean isUsable(String tenant) {
        return enabled && tenant != null && !tenant.isBlank();
    }

    private static String allGroupsKey(String tenant) {
        return KEY_PREFIX + tenant + ":all";
    }

    private static String groupKey(String tenant, Long groupId) {
        return KEY_PREFIX + tenant + ":" + groupId;
    }
}
