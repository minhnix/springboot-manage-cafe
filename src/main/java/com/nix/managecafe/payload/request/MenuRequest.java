package com.nix.managecafe.payload.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class MenuRequest {
    private Long id;
    @NotBlank
    private String name;
    private String description;
    @NotNull
    private Long categoryId;
    @NotNull
    private Long cost;
    private String imageUrl;
    @Valid
    List<MenuDetailRequest> menuDetails;
}
