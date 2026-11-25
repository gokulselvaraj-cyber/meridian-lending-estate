package com.meridian.lending.service;

import com.meridian.lending.domain.LoanApplication;
import com.meridian.lending.repository.ApplicationRepository;
import java.time.Instant;
import java.util.UUID;
import org.springframework.stereotype.Service;

/** Takes in an application, has it underwritten, and stores the outcome. */
@Service
public class OriginationService {
    private final UnderwritingService underwriting;
    private final ApplicationRepository applications;

    public OriginationService(UnderwritingService underwriting, ApplicationRepository applications) {
        this.underwriting = underwriting;
        this.applications = applications;
    }

    public LoanApplication submit(LoanApplication application) {
        application.setApplicationId(UUID.randomUUID().toString());
        application.setSubmittedAt(Instant.now());
        underwriting.assess(application);
        return applications.save(application);
    }

    public java.util.Optional<LoanApplication> find(String applicationId) {
        return applications.findById(applicationId);
    }
}
