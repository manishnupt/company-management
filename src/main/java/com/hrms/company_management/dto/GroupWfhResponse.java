package com.hrms.company_management.dto;

import com.hrms.company_management.utility.DisbursalFrequency;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
public class GroupWfhResponse {
    private Long groupId;
    private String groupName;
    private List<WFHInfo> wfhInfos;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class WFHInfo {
        private int totalDays;
        private boolean expireUnusedDaysAtCycleEnd;
        @Enumerated(EnumType.STRING)
        private DisbursalFrequency disbursalFrequency;
        private String description;
    }
}
