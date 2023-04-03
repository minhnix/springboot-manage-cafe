package com.nix.managecafe.payload.response;

import com.nix.managecafe.model.PurchaseOrder;
import com.nix.managecafe.model.Supplier;
import com.nix.managecafe.model.enumname.PaymentType;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.List;

@Getter
@Setter
public class PurchaseOrderResponse {
    private Long id;
    private List<PurchaseOrderDetailResponse> orderDetails;
    private Supplier supplier;
    private PaymentType paymentType;
    private Long totalCost;
    private Instant createdAt;
}
