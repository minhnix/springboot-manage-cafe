package com.nix.managecafe.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.nix.managecafe.model.audit.DateAudit;
import jakarta.persistence.*;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.ParamDef;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.type.descriptor.java.BooleanJavaType;

@Entity
@Table(name = "warehouses")
@Getter
@Setter
@NoArgsConstructor
@SQLDelete(sql = "UPDATE warehouses SET deleted = true WHERE id=?")
public class Warehouse extends DateAudit {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Double quantity;
    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "ingredient_id", unique = true)
    @JsonIgnore
    private Product product;
    private String status;
    private Long lowQuantity;
    private boolean deleted = Boolean.FALSE;
}
