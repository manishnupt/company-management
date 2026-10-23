package com.hrms.company_management.notice;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/** Unsigned JWTs for tests. The service trusts Kong for signatures, so only the payload matters. */
public final class TestTokens {

    private TestTokens() {
    }

    public static String bearer(String subject, String... roles) {
        String roleJson = Stream.of(roles).map(r -> "\"" + r + "\"").collect(Collectors.joining(","));
        return bearerWithPayload("{\"sub\":\"" + subject + "\",\"realm_access\":{\"roles\":[" + roleJson + "]}}");
    }

    public static String bearerWithPayload(String payloadJson) {
        Base64.Encoder encoder = Base64.getUrlEncoder().withoutPadding();
        String header = encoder.encodeToString("{\"alg\":\"none\"}".getBytes(StandardCharsets.UTF_8));
        String payload = encoder.encodeToString(payloadJson.getBytes(StandardCharsets.UTF_8));
        return "Bearer " + header + "." + payload + ".sig";
    }
}
