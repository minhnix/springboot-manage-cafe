package com.nix.managecafe.payload.response;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class ProfitResponse {
    private Long profit;
    private Long revenue;
    private Long expense;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
}
