package com.meridian.lending.config;

import com.meridian.lending.service.accrual.AccrualEngine;
import com.meridian.lending.service.accrual.CobolAccrualBridge;
import com.meridian.lending.service.accrual.JavaAccrualJob;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Picks the nightly accrual engine at start-up from flag lending.accrual.engine (COBOL or JAVA). See ADR-003. */
@Configuration
public class AccrualConfig {
    @Bean
    AccrualEngine accrualEngine(@Value("${lending.accrual.engine:COBOL}") String engine,
                                CobolAccrualBridge cobol, JavaAccrualJob java) {
        return "JAVA".equalsIgnoreCase(engine) ? java : cobol;
    }
}
