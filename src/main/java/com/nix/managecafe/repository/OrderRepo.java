package com.nix.managecafe.repository;

import com.nix.managecafe.model.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;

public interface OrderRepo extends JpaRepository<Order, Long> {
    Page<Order> findAllByStatus(Pageable pageable, String status);
    Page<Order> findAllByCreatedBy(Pageable pageable, Long userId);
    Page<Order> findByStaffId(Pageable pageable, Long staffId);
    Page<Order> findByCreatedAtBetween( Pageable pageable, LocalDateTime startDate, LocalDateTime endDate);
    Page<Order> findByCreatedAtBetweenAndStatus(Pageable pageable, LocalDateTime startDate, LocalDateTime endDate, String status);
    long countByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);


}
