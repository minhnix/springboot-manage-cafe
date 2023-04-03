package com.nix.managecafe.payload.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CartRequest {
    private Long menuId;
    private Long quantity;
}
