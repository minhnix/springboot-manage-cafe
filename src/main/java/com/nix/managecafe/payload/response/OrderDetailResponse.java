package com.nix.managecafe.payload.response;

import com.nix.managecafe.model.Menu;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class OrderDetailResponse {
    private Long id;
    private String menu;
    private String imageUrl;
    private int quantity;
    private Long menuCost;
    private Long totalCost;
    private String menuSize;
}
