package com.nix.managecafe.payload.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MenuDetailRequest {
    private Long id;
    @NotNull
    private Long productId;
    @NotNull
    private Double quantity;
}
