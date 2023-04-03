package com.nix.managecafe.repository;

import com.nix.managecafe.model.PurchaseOrderDetail;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PurchaseOrderDetailRepo extends JpaRepository<PurchaseOrderDetail, Long> {
}
