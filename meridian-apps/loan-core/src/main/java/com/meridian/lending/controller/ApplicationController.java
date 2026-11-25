package com.meridian.lending.controller;

import com.meridian.lending.domain.LoanApplication;
import com.meridian.lending.origination.LoanValidator;
import com.meridian.lending.rules.AddressValidator;
import com.meridian.lending.service.OriginationService;
import java.math.BigDecimal;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/applications")
public class ApplicationController {
    public record SubmitRequest(String customerId, BigDecimal requestedAmount, BigDecimal propertyValue,
                                int termMonths, String street, String city, String postcode) {
    }

    private final OriginationService origination;
    private final LoanValidator validator;
    private final AddressValidator addresses;

    public ApplicationController(OriginationService origination,
                                 @Qualifier("originationLoanValidator") LoanValidator validator,
                                 AddressValidator addresses) {
        this.origination = origination;
        this.validator = validator;
        this.addresses = addresses;
    }

    @PostMapping
    public ResponseEntity<LoanApplication> submit(@RequestBody SubmitRequest req) {
        validator.validate(req.requestedAmount());
        if (!addresses.isValid(req.street(), req.city(), req.postcode())) {
            return ResponseEntity.badRequest().build();
        }
        LoanApplication a = new LoanApplication();
        a.setCustomerId(req.customerId());
        a.setRequestedAmount(req.requestedAmount());
        a.setPropertyValue(req.propertyValue());
        a.setTermMonths(req.termMonths());
        return ResponseEntity.status(201).body(origination.submit(a));
    }

    @GetMapping("/{id}")
    public ResponseEntity<LoanApplication> get(@PathVariable("id") String id) {
        return ResponseEntity.of(origination.find(id));
    }
}
