package com.nix.managecafe.repository;

import com.nix.managecafe.model.OrderDetail;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderDetailRepo extends JpaRepository<OrderDetail, Long> {
    OrderDetail getOrderDetailsByOrderIdAndMenuId(Long orderId, Long menuId);
    List<OrderDetail> findByOrderId(Long orderId);
}
