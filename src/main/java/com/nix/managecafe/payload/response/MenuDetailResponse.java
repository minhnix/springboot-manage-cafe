package com.nix.managecafe.payload.response;

import com.nix.managecafe.model.Category;
import com.nix.managecafe.model.MenuDetail;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class MenuDetailResponse {
    private Long id;
    private String name;
    private String description;
    private Long cost;
    private String imageUrl;
    private Category category;
    List<MenuDetail> menuDetails;
}
