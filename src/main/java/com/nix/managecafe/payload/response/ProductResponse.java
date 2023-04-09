package com.nix.managecafe.payload.response;

import com.nix.managecafe.model.Warehouse;
import jakarta.persistence.OneToOne;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProductResponse {
    private Long id;
    private String name;
    private String imageUrl;
    private Long cost;
    private String unit;
    private Double quantity;
}
