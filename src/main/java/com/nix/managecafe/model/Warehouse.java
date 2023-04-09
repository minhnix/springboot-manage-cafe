package com.nix.managecafe.model;

import com.nix.managecafe.model.audit.DateAudit;
import jakarta.persistence.*;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.ParamDef;
import org.hibernate.type.descriptor.java.BooleanJavaType;

@Entity
@Table(name = "warehouses")
@Getter
@Setter
@NoArgsConstructor
@FilterDef(name = "deletedWarehouseFilter", parameters = @ParamDef(name = "isDeleted", type = BooleanJavaType.class))
@Filter(name = "deletedWarehouseFilter", condition = "p1_0.deleted = :isDeleted")
public class Warehouse extends DateAudit {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Double quantity;
    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "ingredient_id", unique = true)
    private Product product;
    private String status;
    private Long lowQuantity;
}
