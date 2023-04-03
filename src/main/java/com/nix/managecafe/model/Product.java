package com.nix.managecafe.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.*;
import org.hibernate.type.descriptor.java.BooleanJavaType;

@Entity
@Table(name = "ingredients")
@Getter
@Setter
@NoArgsConstructor
@SQLDelete(sql = "UPDATE ingredients SET deleted = true WHERE id=?")
@FilterDef(name = "deletedProductFilter", parameters = @ParamDef(name = "isDeleted", type = BooleanJavaType.class))
@Filter(name = "deletedProductFilter", condition = "deleted = :isDeleted")
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @NotNull
    private String name;
    private String description;
    private String imageUrl;
    @NotNull
    private Long cost;
    @NotNull
    private String unit;
    @JsonIgnore
    private boolean deleted = Boolean.FALSE;
}
