package com.nix.managecafe.repository;

import com.nix.managecafe.model.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepo extends JpaRepository<Order, Long> {
    Page<Order> findAllByStatus(Pageable pageable, String status);
    Page<Order> findAllByCreatedBy(Pageable pageable, Long userId);
    Page<Order> findAllByStaffId(Pageable pageable, Long staffId);
}
