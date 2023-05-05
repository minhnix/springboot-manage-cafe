package com.nix.managecafe.service;

import com.nix.managecafe.exception.BadRequestException;
import com.nix.managecafe.payload.response.ProfitResponse;
import com.nix.managecafe.repository.OrderRepo;
import com.nix.managecafe.repository.PurchaseOrderRepo;
import com.nix.managecafe.util.ValidateDate;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

@Service
public class AnalyticsService {
    private final PurchaseOrderRepo purchaseOrderRepo;
    private final OrderRepo orderRepo;
    private final EntityManager entityManager;
    private final Logger logger = LoggerFactory.getLogger(AnalyticsService.class);

    public AnalyticsService(PurchaseOrderRepo purchaseOrderRepo, OrderRepo orderRepo, EntityManager entityManager) {
        this.purchaseOrderRepo = purchaseOrderRepo;
        this.orderRepo = orderRepo;
        this.entityManager = entityManager;
    }

    public ProfitResponse getProfit(String startDateString, String endDateString) {
        ProfitResponse profitResponse = new ProfitResponse();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDateTime startDate, endDate;
        try {
            if (startDateString != null && !startDateString.isBlank()) {
                startDate = LocalDate.parse(startDateString, formatter).atStartOfDay();
            } else {
                startDate = LocalDateTime.of(2000, 1, 1, 0, 0, 0);
            }
            if (endDateString != null && !endDateString.isBlank()) {
                endDate = LocalDate.parse(endDateString, formatter).atTime(LocalTime.MAX);
            } else {
                endDate = LocalDateTime.now();
            }
        } catch (DateTimeParseException ex) {
            throw new BadRequestException("Lỗi định dạng ngày tháng (yyyy-MM-dd)");
        }
        ValidateDate.invoke(startDate, endDate);

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

    public List<Object[]> getRevenueByMonthAndYear(int month, int year) {
        if (month < 1 || month > 12) throw new BadRequestException("Tháng không hợp lệ");
        if (year > LocalDate.now().getYear()) throw new BadRequestException("Năm không hợp lệ");
        List<Object[]> list = orderRepo.getRevenueByMonthAndYear(month, year);
        List<Object[]> result = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        for (Object[] o : list) {
            Object[] data = new Object[2];
            data[0] = LocalDate.parse(o[0].toString(), formatter).getDayOfMonth();
            data[1] = o[1];
            result.add(data);
        }
        return result;
    }

    public List<Object[]> getRevenueByYear(int year) {
        if (year > LocalDate.now().getYear()) throw new BadRequestException("Năm không hợp lệ");
        return orderRepo.getRevenueByYear(year);
    }


    public List<Object[]> getRevenueRecent(int dayAgo) {
        LocalDateTime startDate = LocalDateTime.now().minusDays(dayAgo);
        LocalDateTime endDate = LocalDateTime.now();
        return orderRepo.getRevenueRecent(startDate, endDate);
    }

    public List<Object[]> getTopMenuSelling(int dayAgo, int top) {
        LocalDateTime startDate = LocalDateTime.now().minusDays(dayAgo);
        LocalDateTime endDate = LocalDateTime.now();
        return orderRepo.getMenuTopSale(startDate, endDate, top);
    }

    public List<Object[]> getTopCountOfMenuSelling(int dayAgo, int top) {
        LocalDateTime startDate = LocalDateTime.now().minusDays(dayAgo);
        LocalDateTime endDate = LocalDateTime.now();
        return orderRepo.getMenuTopCount(startDate, endDate, top);
    }
}
