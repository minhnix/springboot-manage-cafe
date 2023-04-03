package com.nix.managecafe.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "menu_details")
@Getter
@Setter
@NoArgsConstructor
public class MenuDetail {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JsonIgnore
    private Menu menu;
    @ManyToOne
    @JoinColumn(name = "ingredient_id")
    private Product product;
    private Double quantity;
}
