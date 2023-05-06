package com.nix.managecafe.controller;

import com.nix.managecafe.exception.BadRequestException;
import com.nix.managecafe.payload.response.ProfitResponse;
import com.nix.managecafe.service.AnalyticsService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

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

    @GetMapping("/revenue")
    public List<Object[]> getRevenueRecent(
            @RequestParam(value = "recent", required = false, defaultValue = "0") int dayAgo,
            @RequestParam(value = "y", required = false) Integer year,
            @RequestParam(value = "start", required = false) String start,
            @RequestParam(value = "end", required = false) String end

    ) {
        if (dayAgo < 0) throw new BadRequestException("param recent phải lớn hơn 0");
        if (dayAgo > 0)
            return analyticsService.getRevenueRecent(dayAgo);
        if (year != null)
            return analyticsService.getRevenueByYear(year);
        return analyticsService.getRevenueBetween(start, end);
    }

    @GetMapping("/menu")
    public List<Object[]> getTopMenu(
            @RequestParam(value = "recent", defaultValue = "1") int dayAgo,
            @RequestParam(value = "top", defaultValue = "50") int top,
            @RequestParam(value = "type") String type,
            @RequestParam(value = "start", required = false) String start,
            @RequestParam(value = "end", required = false) String end
    ) {
        if ("sale".equals(type))
            return analyticsService.getTopMenuSelling(dayAgo, top);
        else if ("count".equals(type))
            return analyticsService.getTopCountOfMenuSelling(dayAgo, top);
        else if ("all".equals(type))
            return analyticsService.getTopMenu(start, end, top);
        else
            throw new BadRequestException("param type must be `sale` or `count` or `all`");
    }

}
