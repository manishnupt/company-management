package com.hrms.company_management.utility;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class PolicyHelper {
    public static Map<String, Map<String, String>> extractPolicyData(Map<String, Object> policyRules) {
        Map<String, Map<String, String>> policyData = new LinkedHashMap<>();

        for (Map.Entry<String, Object> policyEntry : policyRules.entrySet()) {
            String policyName = policyEntry.getKey();
            Object attributesObj = policyEntry.getValue();

            if (attributesObj instanceof Map<?, ?> attributesMap) {
                Map<String, String> flattenedAttributes = new LinkedHashMap<>();

                for (Map.Entry<?, ?> attrEntry : attributesMap.entrySet()) {
                    String attrKey = attrEntry.getKey().toString();
                    Object attrVal = attrEntry.getValue();

                    if (attrVal instanceof List<?> listVal) {
                        String joined = listVal.stream()
                                .map(Object::toString)
                                .collect(Collectors.joining(","));
                        flattenedAttributes.put(attrKey, joined);
                    } else {
                        flattenedAttributes.put(attrKey, attrVal != null ? attrVal.toString() : null);
                    }
                }

                policyData.put(policyName, flattenedAttributes);
            }
        }

        return policyData;
    }
}
