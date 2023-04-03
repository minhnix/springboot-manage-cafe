package com.nix.managecafe.payload.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.Value;

@Getter
@Setter
@NoArgsConstructor
public class OrderDetailRequest {
    private Long orderId;
    @NotNull
    private Long menuId;
    @NotNull
    private int quantity;
    @NotNull
    private String size;
}
