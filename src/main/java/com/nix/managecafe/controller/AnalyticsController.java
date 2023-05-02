package com.nix.managecafe.controller;

import com.nix.managecafe.payload.response.ProfitResponse;
import com.nix.managecafe.service.AnalyticsService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/analytics")
@PreAuthorize("hasRole('ADMIN')")
public class AnalyticsController {
    private final AnalyticsService analyticsService;

    public AnalyticsController(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    @GetMapping("/profit")
    public ProfitResponse getProfit(
        @RequestParam(value = "start", required = false) String start,
        @RequestParam(value = "end", required = false) String end
    ) {
        return analyticsService.getProfit(start, end);
    }
}
