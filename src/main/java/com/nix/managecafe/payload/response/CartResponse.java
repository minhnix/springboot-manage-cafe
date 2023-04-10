package com.nix.managecafe.payload.response;

import com.nix.managecafe.model.Menu;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CartResponse {
    private Long id;
    private String name;
    private Long menuId;
    private Long quantity;
    private String imageUrl;
    private String size;
    private Long totalCost;
    private boolean deleted;
}
