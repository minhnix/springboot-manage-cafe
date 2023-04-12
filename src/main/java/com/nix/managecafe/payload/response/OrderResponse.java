package com.nix.managecafe.payload.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrderResponse {
    private Long id;
    private List<OrderDetailResponse> orderDetails;
    private UserResponse customer;
    private UserResponse staff;
    private String address;
    private String status;
    private String note;
    private Long subTotalCost;
    private Long deliveryCost;
    private Long amountDiscount;
    private Long totalCost;
    private LocalDateTime createdAt;
}
