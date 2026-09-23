package com.hrms.company_management.service;

import com.hrms.company_management.dto.AttendanceRuleRequest;
import com.hrms.company_management.dto.AttendanceRuleResponse;
import com.hrms.company_management.dto.CreatePolicyRequest;
import com.hrms.company_management.dto.EmployeeLifecycleRequest;
import com.hrms.company_management.dto.EmployeeLifecycleResponse;
import com.hrms.company_management.dto.MiscellaneousRequest;
import com.hrms.company_management.dto.MiscellaneousResponse;
import com.hrms.company_management.dto.PolicyResponse;
import com.hrms.company_management.dto.WorkScheduleRequest;
import com.hrms.company_management.dto.WorkScheduleResponse;
import com.hrms.company_management.entity.Policy;
import com.hrms.company_management.entity.PolicyAttendanceRule;
import com.hrms.company_management.entity.PolicyEmployeeLifecycle;
import com.hrms.company_management.entity.PolicyMiscellaneous;
import com.hrms.company_management.entity.PolicyWorkSchedule;
import com.hrms.company_management.repository.PoliciesRepository;
import com.hrms.company_management.repository.PolicyAttendanceRuleRepository;
import com.hrms.company_management.repository.PolicyEmployeeLifecycleRepository;
import com.hrms.company_management.repository.PolicyMiscellaneousRepository;
import com.hrms.company_management.repository.PolicyWorkScheduleRepository;
import com.hrms.company_management.utility.PolicyStatus;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashSet;
import java.util.List;
import java.util.UUID;

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

    // create
    public PolicyResponse createPolicy(CreatePolicyRequest request) {
        log.info("Creating policy: {}", request);
        Policy policy = new Policy();
        policy.setName(request.getName());
        policy.setDescription(request.getDescription());
        policy.setEffectiveFrom(request.getEffectiveFrom());
        policy.setVersion(1);
        policy.setStatus(PolicyStatus.DRAFT);

        Policy saved = repository.save(policy);
        return toResponse(saved);
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

    // create or replace work schedule
    @Transactional
    public WorkScheduleResponse upsertWorkSchedule(UUID policyId, WorkScheduleRequest request) {
        log.info("Upserting work schedule for policy {}: {}", policyId, request);
        Policy policy = getPolicyEntityById(policyId);

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

    // create or replace attendance rules
    @Transactional
    public AttendanceRuleResponse upsertAttendanceRule(UUID policyId, AttendanceRuleRequest request) {
        log.info("Upserting attendance rules for policy {}: {}", policyId, request);
        Policy policy = getPolicyEntityById(policyId);

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

    // create or replace employee lifecycle
    @Transactional
    public EmployeeLifecycleResponse upsertEmployeeLifecycle(UUID policyId, EmployeeLifecycleRequest request) {
        log.info("Upserting employee lifecycle for policy {}: {}", policyId, request);
        Policy policy = getPolicyEntityById(policyId);

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
        Policy policy = getPolicyEntityById(policyId);

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
                .status(policy.getStatus())
                .effectiveFrom(policy.getEffectiveFrom())
                .effectiveTo(policy.getEffectiveTo())
                .build();
    }
}
