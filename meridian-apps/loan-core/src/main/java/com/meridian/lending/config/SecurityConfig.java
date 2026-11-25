package com.meridian.lending.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Roles:
 *   APPLICANT       submits applications and views their own
 *   UNDERWRITER     views applications
 *   SERVICING_AGENT records payments
 *   ADMIN           everything
 */
@Configuration
public class SecurityConfig {
    @Bean
    SecurityFilterChain api(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(a -> a
                .requestMatchers(HttpMethod.POST, "/api/v1/quotes").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/v1/applications").hasAnyRole("APPLICANT", "ADMIN")
                .requestMatchers(HttpMethod.GET, "/api/v1/applications/**")
                        .hasAnyRole("APPLICANT", "UNDERWRITER", "ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/v1/loans/*/payments").hasAnyRole("SERVICING_AGENT", "ADMIN")
                .requestMatchers(HttpMethod.GET, "/api/v1/loans/**").hasAnyRole("APPLICANT", "SERVICING_AGENT", "ADMIN")
                .requestMatchers("/actuator/health/**").permitAll()
                .anyRequest().hasRole("ADMIN"))
            .oauth2ResourceServer(o -> o.jwt(j -> { }));
        return http.build();
    }
}
