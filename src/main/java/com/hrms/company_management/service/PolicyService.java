package com.hrms.company_management.service;

import com.hrms.company_management.dto.AttendanceRuleRequest;
import com.hrms.company_management.dto.AttendanceRuleResponse;
import com.hrms.company_management.dto.CreatePolicyRequest;
import com.hrms.company_management.dto.EmployeeLifecycleRequest;
import com.hrms.company_management.dto.EmployeeLifecycleResponse;
import com.hrms.company_management.dto.GroupResponse;
import com.hrms.company_management.dto.MiscellaneousRequest;
import com.hrms.company_management.dto.MiscellaneousResponse;
import com.hrms.company_management.dto.PolicyResponse;
import com.hrms.company_management.dto.PolicyValidationResponse;
import com.hrms.company_management.dto.UpdatePolicyRequest;
import com.hrms.company_management.dto.WorkScheduleRequest;
import com.hrms.company_management.dto.WorkScheduleResponse;
import com.hrms.company_management.entity.LeaveType;
import com.hrms.company_management.entity.Policy;
import com.hrms.company_management.entity.PolicyAttendanceRule;
import com.hrms.company_management.entity.PolicyEmployeeLifecycle;
import com.hrms.company_management.entity.PolicyMiscellaneous;
import com.hrms.company_management.entity.PolicyWorkSchedule;
import com.hrms.company_management.entity.RoleGroup;
import com.hrms.company_management.entity.WFHType;
import com.hrms.company_management.repository.LeavePolicyRepository;
import com.hrms.company_management.repository.PoliciesRepository;
import com.hrms.company_management.repository.PolicyAttendanceRuleRepository;
import com.hrms.company_management.repository.PolicyEmployeeLifecycleRepository;
import com.hrms.company_management.repository.PolicyMiscellaneousRepository;
import com.hrms.company_management.repository.PolicyWorkScheduleRepository;
import com.hrms.company_management.repository.RoleGroupRepo;
import com.hrms.company_management.repository.WFHRepository;
import com.hrms.company_management.utility.PolicyStatus;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Log4j2
public class PolicyService {

    @Autowired
    private PoliciesRepository repository;

    @Autowired
    private PolicyWorkScheduleRepository workScheduleRepository;

    @Autowired
    private PolicyAttendanceRuleRepository attendanceRuleRepository;

    @Autowired
    private PolicyEmployeeLifecycleRepository employeeLifecycleRepository;

    @Autowired
    private PolicyMiscellaneousRepository miscellaneousRepository;

    @Autowired
    private LeavePolicyRepository leaveTypeRepository;

    @Autowired
    private WFHRepository wfhRepository;

    @Autowired
    private RoleGroupRepo roleGroupRepo;

    // create
    @Transactional
    public PolicyResponse createPolicy(CreatePolicyRequest request) {
        log.info("Creating policy: {}", request);
        String name = request.getName().trim();
        requireUniqueNameAndVersion(name, 1, null);

        Policy policy = new Policy();
        policy.setName(name);
        policy.setDescription(request.getDescription());
        policy.setEffectiveFrom(request.getEffectiveFrom());
        policy.setVersion(1);
        policy.setStatus(PolicyStatus.DRAFT);

        Policy saved = repository.save(policy);
        saved.setRootPolicyId(saved.getId());
        return toResponse(saved);
    }

    // create a new DRAFT version by copying an existing version; the source is left unchanged
    @Transactional
    public PolicyResponse createNewVersion(UUID policyId) {
        log.info("Creating new version from policy: {}", policyId);
        Policy source = getPolicyEntityById(policyId);

        if (source.getStatus() == PolicyStatus.DRAFT) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Policy " + policyId + " is still a DRAFT; edit it directly instead of creating a new version");
        }

        // policies created before versioning have no root; they are their own root
        if (source.getRootPolicyId() == null) {
            source.setRootPolicyId(source.getId());
        }
        UUID rootPolicyId = source.getRootPolicyId();

        // lock the root row so concurrent requests for the same policy cannot both create a draft
        repository.findByIdForUpdate(rootPolicyId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Root policy not found with id: " + rootPolicyId));

        if (repository.existsByRootPolicyIdAndStatus(rootPolicyId, PolicyStatus.DRAFT)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "A DRAFT version already exists for policy " + rootPolicyId);
        }

        int nextVersion = repository.findMaxVersionByRootPolicyId(rootPolicyId)
                .orElse(source.getVersion()) + 1;
        requireUniqueNameAndVersion(source.getName(), nextVersion, null);

        Policy draft = new Policy();
        draft.setName(source.getName());
        draft.setDescription(source.getDescription());
        draft.setEffectiveFrom(source.getEffectiveFrom());
        draft.setVersion(nextVersion);
        draft.setStatus(PolicyStatus.DRAFT);
        draft.setRootPolicyId(rootPolicyId);
        Policy saved = repository.save(draft);

        copyCards(source.getId(), saved);
        log.info("Created version {} ({}) from policy {}", nextVersion, saved.getId(), policyId);
        return toResponse(saved);
    }

    private void copyCards(UUID sourceId, Policy target) {
        workScheduleRepository.findByPolicyId(sourceId).ifPresent(src -> {
            PolicyWorkSchedule copy = new PolicyWorkSchedule();
            copy.setPolicy(target);
            copy.setCheckInTime(src.getCheckInTime());
            copy.setCheckOutTime(src.getCheckOutTime());
            copy.setMinimumWorkingHours(src.getMinimumWorkingHours());
            copy.setWorkingDays(src.getWorkingDays() == null ? null : new HashSet<>(src.getWorkingDays()));
            workScheduleRepository.save(copy);
        });

        attendanceRuleRepository.findByPolicyId(sourceId).ifPresent(src -> {
            PolicyAttendanceRule copy = new PolicyAttendanceRule();
            copy.setPolicy(target);
            copy.setLateMarkThresholdMinutes(src.getLateMarkThresholdMinutes());
            copy.setConsecutiveLateDaysAllowed(src.getConsecutiveLateDaysAllowed());
            copy.setAutoMarkAbsentAfterHours(src.getAutoMarkAbsentAfterHours());
            attendanceRuleRepository.save(copy);
        });

        employeeLifecycleRepository.findByPolicyId(sourceId).ifPresent(src -> {
            PolicyEmployeeLifecycle copy = new PolicyEmployeeLifecycle();
            copy.setPolicy(target);
            copy.setProbationPeriodDays(src.getProbationPeriodDays());
            copy.setNoticePeriodDays(src.getNoticePeriodDays());
            employeeLifecycleRepository.save(copy);
        });

        miscellaneousRepository.findByPolicyId(sourceId).ifPresent(src -> {
            PolicyMiscellaneous copy = new PolicyMiscellaneous();
            copy.setPolicy(target);
            copy.setDressCode(src.getDressCode());
            miscellaneousRepository.save(copy);
        });

        List<LeaveType> leaveTypes = leaveTypeRepository.findByPolicy_Id(sourceId).stream()
                .map(src -> {
                    LeaveType copy = new LeaveType();
                    copy.setId(UUID.randomUUID().toString());
                    copy.setPolicy(target);
                    copy.setName(src.getName());
                    copy.setTotalDays(src.getTotalDays());
                    copy.setCarryForward(src.isCarryForward());
                    copy.setDisbursalFrequency(src.getDisbursalFrequency());
                    copy.setDescription(src.getDescription());
                    return copy;
                })
                .toList();
        leaveTypeRepository.saveAll(leaveTypes);

        List<WFHType> wfhTypes = wfhRepository.findByPolicy_Id(sourceId).stream()
                .map(src -> {
                    WFHType copy = new WFHType();
                    copy.setId(UUID.randomUUID().toString());
                    copy.setPolicy(target);
                    copy.setName(src.getName());
                    copy.setDescription(src.getDescription());
                    copy.setTotalDays(src.getTotalDays());
                    copy.setDisbursalFrequency(src.getDisbursalFrequency());
                    return copy;
                })
                .toList();
        wfhRepository.saveAll(wfhTypes);
    }

    // get all
    public List<PolicyResponse> getAllPolicies() {
        log.info("Fetching all policies");
        return repository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    // get by id
    public PolicyResponse getPolicyById(UUID policyId) {
        log.info("Fetching policy with id: {}", policyId);
        return toResponse(getPolicyEntityById(policyId));
    }

    // get entity by id
    public Policy getPolicyEntityById(UUID policyId) {
        return repository.findById(policyId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Policy not found with id: " + policyId));
    }

    // update policy details; only a DRAFT version can be changed
    @Transactional
    public PolicyResponse updatePolicy(UUID policyId, UpdatePolicyRequest request) {
        log.info("Updating policy {}: {}", policyId, request);
        Policy policy = getDraftPolicyEntityById(policyId);

        if (request.getEffectiveTo() != null && request.getEffectiveTo().isBefore(request.getEffectiveFrom())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "effectiveTo cannot be before effectiveFrom");
        }

        String name = request.getName().trim();
        requireUniqueNameAndVersion(name, policy.getVersion(), policyId);

        policy.setName(name);
        policy.setDescription(request.getDescription());
        policy.setEffectiveFrom(request.getEffectiveFrom());
        policy.setEffectiveTo(request.getEffectiveTo());
        policy.setUpdatedAt(LocalDateTime.now());
        return toResponse(policy);
    }

    // (name, version) must be unique across policies; excludeId skips the policy being updated
    private void requireUniqueNameAndVersion(String name, Integer version, UUID excludeId) {
        boolean exists = excludeId == null
                ? repository.existsByNameIgnoreCaseAndVersion(name, version)
                : repository.existsByNameIgnoreCaseAndVersionAndIdNot(name, version, excludeId);
        if (exists) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "A policy named '" + name + "' with version " + version + " already exists");
        }
    }

    // get entity by id, only if it is still a DRAFT; published versions are read-only
    public Policy getDraftPolicyEntityById(UUID policyId) {
        Policy policy = getPolicyEntityById(policyId);
        requireDraft(policy);
        return policy;
    }

    public void requireDraft(Policy policy) {
        if (policy.getStatus() != PolicyStatus.DRAFT) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Policy " + policy.getId() + " is " + policy.getStatus()
                            + " and cannot be modified; create a new version instead");
        }
    }

    // check whether a policy is complete enough to publish
    public PolicyValidationResponse validatePolicy(UUID policyId) {
        log.info("Validating policy {}", policyId);
        return validate(getPolicyEntityById(policyId));
    }

    private PolicyValidationResponse validate(Policy policy) {
        UUID policyId = policy.getId();
        List<String> errors = new ArrayList<>();
        List<String> warnings = new ArrayList<>();

        if (policy.getEffectiveFrom() == null) {
            errors.add("effectiveFrom is not set");
        } else {
            if (policy.getEffectiveTo() != null && policy.getEffectiveTo().isBefore(policy.getEffectiveFrom())) {
                errors.add("effectiveTo is before effectiveFrom");
            }
            if (policy.getStatus() == PolicyStatus.DRAFT && policy.getEffectiveFrom().isBefore(LocalDate.now())) {
                warnings.add("effectiveFrom " + policy.getEffectiveFrom() + " is in the past");
            }
        }

        workScheduleRepository.findByPolicyId(policyId).ifPresentOrElse(schedule -> {
            long shiftMinutes = Duration.between(schedule.getCheckInTime(), schedule.getCheckOutTime()).toMinutes();
            if (shiftMinutes < 0) {
                shiftMinutes += 24 * 60; // overnight shift
            }
            BigDecimal shiftHours = BigDecimal.valueOf(shiftMinutes).divide(BigDecimal.valueOf(60), 2, RoundingMode.HALF_UP);
            if (schedule.getMinimumWorkingHours().compareTo(shiftHours) > 0) {
                errors.add("Work schedule: minimumWorkingHours (" + schedule.getMinimumWorkingHours()
                        + ") is longer than the shift (" + shiftHours + " hours)");
            }
        }, () -> errors.add("Work schedule is not configured"));

        if (attendanceRuleRepository.findByPolicyId(policyId).isEmpty()) {
            errors.add("Attendance rules are not configured");
        }
        if (employeeLifecycleRepository.findByPolicyId(policyId).isEmpty()) {
            errors.add("Employee lifecycle is not configured");
        }
        if (leaveTypeRepository.findByPolicy_Id(policyId).isEmpty()) {
            errors.add("No leave types are configured");
        }
        if (miscellaneousRepository.findByPolicyId(policyId).isEmpty()) {
            warnings.add("Miscellaneous settings are not configured");
        }
        if (wfhRepository.findByPolicy_Id(policyId).isEmpty()) {
            warnings.add("No WFH types are configured");
        }

        return PolicyValidationResponse.builder()
                .policyId(policyId)
                .status(policy.getStatus())
                .valid(errors.isEmpty())
                .errors(errors)
                .warnings(warnings)
                .build();
    }

    // publish a DRAFT version; other versions of the same policy are left unchanged
    @Transactional
    public PolicyResponse publish(UUID policyId) {
        log.info("Publishing policy: {}", policyId);
        Policy policy = getPolicyEntityById(policyId);
        requireDraft(policy);

        PolicyValidationResponse validation = validate(policy);
        if (!validation.isValid()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Policy " + policyId + " cannot be published: " + String.join("; ", validation.getErrors()));
        }

        UUID rootPolicyId = policy.getRootPolicyId() == null ? policy.getId() : policy.getRootPolicyId();
        policy.setRootPolicyId(rootPolicyId);

        // serialise with createNewVersion on the same root
        repository.findByIdForUpdate(rootPolicyId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Root policy not found with id: " + rootPolicyId));

        policy.setStatus(PolicyStatus.ACTIVE);
        policy.setUpdatedAt(LocalDateTime.now());
        log.info("Published version {} ({}) of policy {}", policy.getVersion(), policyId, rootPolicyId);
        return toResponse(policy);
    }

    // move groups onto a published version; groups on another version of this policy are switched over
    @Transactional
    public List<GroupResponse> assignGroups(UUID policyId, List<Long> groupIds) {
        log.info("Assigning groups {} to policy {}", groupIds, policyId);
        Policy policy = getPolicyEntityById(policyId);
        if (policy.getStatus() != PolicyStatus.ACTIVE) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Policy " + policyId + " is " + policy.getStatus() + "; groups can only be moved to a published (ACTIVE) version");
        }

        List<RoleGroup> groups = roleGroupRepo.findAllById(groupIds);
        if (groups.size() != new HashSet<>(groupIds).size()) {
            Set<Long> found = groups.stream().map(RoleGroup::getId).collect(Collectors.toSet());
            List<Long> missing = groupIds.stream().filter(id -> !found.contains(id)).distinct().toList();
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Groups not found: " + missing);
        }

        groups.forEach(group -> group.setPolicy(policy));
        roleGroupRepo.saveAll(groups);
        return groups.stream().map(this::toGroupResponse).toList();
    }

    // move a single group onto a published version
    @Transactional
    public PolicyResponse assignPolicyToGroup(Long groupId, UUID policyId) {
        log.info("Assigning policy {} to group {}", policyId, groupId);
        Policy policy = getPolicyEntityById(policyId);
        if (policy.getStatus() != PolicyStatus.ACTIVE) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Policy " + policyId + " is " + policy.getStatus() + "; groups can only be moved to a published (ACTIVE) version");
        }

        RoleGroup group = getGroupEntityById(groupId);
        group.setPolicy(policy);
        roleGroupRepo.save(group);
        return toResponse(policy);
    }

    // policy version the group currently follows
    @Transactional(readOnly = true)
    public PolicyResponse getGroupPolicy(Long groupId) {
        log.info("Fetching policy for group {}", groupId);
        Policy policy = getGroupEntityById(groupId).getPolicy();
        if (policy == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No policy assigned to group: " + groupId);
        }
        return toResponse(policy);
    }

    private RoleGroup getGroupEntityById(Long groupId) {
        return roleGroupRepo.findById(groupId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Group not found with id: " + groupId));
    }

    // groups currently on this policy version
    public List<GroupResponse> getGroups(UUID policyId) {
        log.info("Fetching groups for policy {}", policyId);
        getPolicyEntityById(policyId);
        return roleGroupRepo.findByPolicy_Id(policyId).stream()
                .map(this::toGroupResponse)
                .toList();
    }

    private GroupResponse toGroupResponse(RoleGroup group) {
        GroupResponse response = new GroupResponse();
        response.setGroupId(group.getId());
        response.setGroupName(group.getName());
        response.setGroupDescription(group.getDescription());
        return response;
    }

    // create or replace work schedule
    @Transactional
    public WorkScheduleResponse upsertWorkSchedule(UUID policyId, WorkScheduleRequest request) {
        log.info("Upserting work schedule for policy {}: {}", policyId, request);
        Policy policy = getDraftPolicyEntityById(policyId);

        if (request.getCheckInTime().equals(request.getCheckOutTime())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "checkInTime and checkOutTime cannot be the same");
        }

        PolicyWorkSchedule schedule = workScheduleRepository.findByPolicyId(policyId)
                .orElseGet(() -> {
                    PolicyWorkSchedule created = new PolicyWorkSchedule();
                    created.setPolicy(policy);
                    return created;
                });
        schedule.setCheckInTime(request.getCheckInTime());
        schedule.setCheckOutTime(request.getCheckOutTime());
        schedule.setMinimumWorkingHours(request.getMinimumWorkingHours());
        schedule.setWorkingDays(new HashSet<>(request.getWorkingDays()));

        PolicyWorkSchedule saved = workScheduleRepository.save(schedule);
        return toWorkScheduleResponse(saved, policyId);
    }

    // get work schedule
    public WorkScheduleResponse getWorkSchedule(UUID policyId) {
        log.info("Fetching work schedule for policy {}", policyId);
        getPolicyEntityById(policyId);

        PolicyWorkSchedule schedule = workScheduleRepository.findByPolicyId(policyId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Work schedule not configured for policy: " + policyId));
        return toWorkScheduleResponse(schedule, policyId);
    }

    // create or replace attendance rules
    @Transactional
    public AttendanceRuleResponse upsertAttendanceRule(UUID policyId, AttendanceRuleRequest request) {
        log.info("Upserting attendance rules for policy {}: {}", policyId, request);
        Policy policy = getDraftPolicyEntityById(policyId);

        PolicyAttendanceRule rule = attendanceRuleRepository.findByPolicyId(policyId)
                .orElseGet(() -> {
                    PolicyAttendanceRule created = new PolicyAttendanceRule();
                    created.setPolicy(policy);
                    return created;
                });
        rule.setLateMarkThresholdMinutes(request.getLateMarkThresholdMinutes());
        rule.setConsecutiveLateDaysAllowed(request.getConsecutiveLateDaysAllowed());
        rule.setAutoMarkAbsentAfterHours(request.getAutoMarkAbsentAfterHours());

        PolicyAttendanceRule saved = attendanceRuleRepository.save(rule);
        return toAttendanceRuleResponse(saved, policyId);
    }

    // get attendance rules
    public AttendanceRuleResponse getAttendanceRule(UUID policyId) {
        log.info("Fetching attendance rules for policy {}", policyId);
        getPolicyEntityById(policyId);

        PolicyAttendanceRule rule = attendanceRuleRepository.findByPolicyId(policyId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Attendance rules not configured for policy: " + policyId));
        return toAttendanceRuleResponse(rule, policyId);
    }

    // create or replace employee lifecycle
    @Transactional
    public EmployeeLifecycleResponse upsertEmployeeLifecycle(UUID policyId, EmployeeLifecycleRequest request) {
        log.info("Upserting employee lifecycle for policy {}: {}", policyId, request);
        Policy policy = getDraftPolicyEntityById(policyId);

        PolicyEmployeeLifecycle lifecycle = employeeLifecycleRepository.findByPolicyId(policyId)
                .orElseGet(() -> {
                    PolicyEmployeeLifecycle created = new PolicyEmployeeLifecycle();
                    created.setPolicy(policy);
                    return created;
                });
        lifecycle.setProbationPeriodDays(request.getProbationPeriodDays());
        lifecycle.setNoticePeriodDays(request.getNoticePeriodDays());

        PolicyEmployeeLifecycle saved = employeeLifecycleRepository.save(lifecycle);
        return toEmployeeLifecycleResponse(saved, policyId);
    }

    // get employee lifecycle
    public EmployeeLifecycleResponse getEmployeeLifecycle(UUID policyId) {
        log.info("Fetching employee lifecycle for policy {}", policyId);
        getPolicyEntityById(policyId);

        PolicyEmployeeLifecycle lifecycle = employeeLifecycleRepository.findByPolicyId(policyId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Employee lifecycle not configured for policy: " + policyId));
        return toEmployeeLifecycleResponse(lifecycle, policyId);
    }

    // create or replace miscellaneous
    @Transactional
    public MiscellaneousResponse upsertMiscellaneous(UUID policyId, MiscellaneousRequest request) {
        log.info("Upserting miscellaneous for policy {}: {}", policyId, request);
        Policy policy = getDraftPolicyEntityById(policyId);

        PolicyMiscellaneous miscellaneous = miscellaneousRepository.findByPolicyId(policyId)
                .orElseGet(() -> {
                    PolicyMiscellaneous created = new PolicyMiscellaneous();
                    created.setPolicy(policy);
                    return created;
                });
        miscellaneous.setDressCode(request.getDressCode());

        PolicyMiscellaneous saved = miscellaneousRepository.save(miscellaneous);
        return toMiscellaneousResponse(saved, policyId);
    }

    // get miscellaneous
    public MiscellaneousResponse getMiscellaneous(UUID policyId) {
        log.info("Fetching miscellaneous for policy {}", policyId);
        getPolicyEntityById(policyId);

        PolicyMiscellaneous miscellaneous = miscellaneousRepository.findByPolicyId(policyId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Miscellaneous not configured for policy: " + policyId));
        return toMiscellaneousResponse(miscellaneous, policyId);
    }

    private MiscellaneousResponse toMiscellaneousResponse(PolicyMiscellaneous miscellaneous, UUID policyId) {
        return MiscellaneousResponse.builder()
                .id(miscellaneous.getId())
                .policyId(policyId)
                .dressCode(miscellaneous.getDressCode())
                .build();
    }

    private EmployeeLifecycleResponse toEmployeeLifecycleResponse(PolicyEmployeeLifecycle lifecycle, UUID policyId) {
        return EmployeeLifecycleResponse.builder()
                .id(lifecycle.getId())
                .policyId(policyId)
                .probationPeriodDays(lifecycle.getProbationPeriodDays())
                .noticePeriodDays(lifecycle.getNoticePeriodDays())
                .build();
    }

    private AttendanceRuleResponse toAttendanceRuleResponse(PolicyAttendanceRule rule, UUID policyId) {
        return AttendanceRuleResponse.builder()
                .id(rule.getId())
                .policyId(policyId)
                .lateMarkThresholdMinutes(rule.getLateMarkThresholdMinutes())
                .consecutiveLateDaysAllowed(rule.getConsecutiveLateDaysAllowed())
                .autoMarkAbsentAfterHours(rule.getAutoMarkAbsentAfterHours())
                .build();
    }

    private WorkScheduleResponse toWorkScheduleResponse(PolicyWorkSchedule schedule, UUID policyId) {
        return WorkScheduleResponse.builder()
                .id(schedule.getId())
                .policyId(policyId)
                .checkInTime(schedule.getCheckInTime())
                .checkOutTime(schedule.getCheckOutTime())
                .minimumWorkingHours(schedule.getMinimumWorkingHours())
                .workingDays(schedule.getWorkingDays())
                .build();
    }

    private PolicyResponse toResponse(Policy policy) {
        return PolicyResponse.builder()
                .id(policy.getId())
                .name(policy.getName())
                .description(policy.getDescription())
                .version(policy.getVersion())
                .rootPolicyId(policy.getRootPolicyId())
                .status(policy.getStatus())
                .effectiveFrom(policy.getEffectiveFrom())
                .effectiveTo(policy.getEffectiveTo())
                .build();
    }
}
