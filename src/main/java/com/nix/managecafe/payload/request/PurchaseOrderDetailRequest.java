package com.nix.managecafe.payload.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class PurchaseOrderDetailRequest {
    @NotNull
    private Long productId;
    @NotNull
    private Long quantity;
}
