package com.meridian.lending.controller;

import com.meridian.lending.service.QuoteService;
import java.math.BigDecimal;
import java.util.Map;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/quotes")
public class QuoteController {
    public record QuoteRequest(int statedScore, BigDecimal amount, BigDecimal propertyValue) {
    }

    private final QuoteService quotes;

    public QuoteController(QuoteService quotes) {
        this.quotes = quotes;
    }

    @PostMapping
    public Map<String, BigDecimal> quote(@RequestBody QuoteRequest req) {
        return Map.of("rate", quotes.quote(req.statedScore(), req.amount(), req.propertyValue()));
    }
}
