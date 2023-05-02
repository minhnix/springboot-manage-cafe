package com.nix.managecafe.service;

import com.nix.managecafe.exception.BadRequestException;
import com.nix.managecafe.payload.response.ProfitResponse;
import com.nix.managecafe.repository.OrderRepo;
import com.nix.managecafe.repository.PurchaseOrderRepo;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

@Service
public class AnalyticsService {
    private final PurchaseOrderRepo purchaseOrderRepo;
    private final OrderRepo orderRepo;

    public AnalyticsService(PurchaseOrderRepo purchaseOrderRepo, OrderRepo orderRepo) {
        this.purchaseOrderRepo = purchaseOrderRepo;
        this.orderRepo = orderRepo;
    }

    public ProfitResponse getProfit(String startDateString, String endDateString) {
        ProfitResponse profitResponse = new ProfitResponse();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDateTime startDate, endDate;
        try {
            if (startDateString != null) {
                startDate = LocalDate.parse(startDateString, formatter).atStartOfDay();
            } else {
                startDate = LocalDateTime.of(2000, 1, 1, 0, 0, 0);
            }
            if (endDateString != null) {
                endDate = LocalDate.parse(endDateString, formatter).atTime(LocalTime.MAX);
            } else {
                endDate = LocalDateTime.now();
            }
        } catch (DateTimeParseException ex) {
            throw new BadRequestException("Lỗi định dạng ngày tháng (yyyy-MM-dd)");
        }

        long expense = purchaseOrderRepo.sumExpense(startDate, endDate);
        long revenue = orderRepo.sumRevenue(startDate, endDate);
        long profit = revenue - expense;
        profitResponse.setExpense(expense);
        profitResponse.setProfit(profit);
        profitResponse.setRevenue(revenue);
        profitResponse.setEndDate(endDate);
        profitResponse.setStartDate(startDate);
        return profitResponse;
    }
}
