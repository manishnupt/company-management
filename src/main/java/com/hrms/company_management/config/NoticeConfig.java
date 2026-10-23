package com.hrms.company_management.config;

import java.time.Clock;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableScheduling
public class NoticeConfig {

    /** Injected so lifecycle logic can be tested with a fixed time. */
    @Bean
    public Clock clock() {
        return Clock.systemUTC();
    }
}
