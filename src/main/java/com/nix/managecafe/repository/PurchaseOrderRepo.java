package com.nix.managecafe.repository;

import com.nix.managecafe.model.PurchaseOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;

public interface PurchaseOrderRepo extends JpaRepository<PurchaseOrder, Long> {
    @Query(value = "SELECT COALESCE (SUM(d.cost * d.quantity), 0) as total from purchase_orders p join purchase_order_details as d on p.id = d.purchase_order_id where p.created_at between ?1 and ?2",
            nativeQuery = true)
    long sumExpense(LocalDateTime start, LocalDateTime end);
}
