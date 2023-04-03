package com.nix.managecafe.payload.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PurchaseOrderDetailResponse {
    private Long id;
    private String productName;
    private String imageUrl;
    private String unit;
    private Long quantity;
    private Long productCost;
    private Long totalCost;
}
