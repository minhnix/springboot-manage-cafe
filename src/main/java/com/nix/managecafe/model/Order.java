package com.nix.managecafe.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.nix.managecafe.model.audit.UserAudit;
import com.nix.managecafe.model.enumname.StatusName;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@jakarta.persistence.Table(name = "orders")
@Getter
@Setter
@NoArgsConstructor
public class Order extends UserAudit {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @OneToMany(mappedBy = "order", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    List<OrderDetail> orderDetails = new ArrayList<>();

    public void addOrderDetails(OrderDetail orderDetail) {
        orderDetails.add(orderDetail);
        orderDetail.setOrder(this);
    }

    public void removeOrderDetails(OrderDetail orderDetail) {
        orderDetails.remove(orderDetail);
        orderDetail.setOrder(null);
    }
    @ManyToOne
    @JoinColumn(name = "customer_id")
    private User customer;
    @ManyToOne
    @JoinColumn(name = "staff_id")
    private User staff;
    private String status;
    private String address;
    private String note;
    @Column(columnDefinition = "BIGINT default 0")
    private Long deliveryCost;
    @Column(columnDefinition = "BIGINT default 0")
    private Long amountDiscount;
}