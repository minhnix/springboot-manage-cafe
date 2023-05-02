package com.nix.managecafe.repository;

import com.nix.managecafe.model.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;

public interface OrderRepo extends JpaRepository<Order, Long> {
    Page<Order> findAllByStatus(Pageable pageable, String status);
    Page<Order> findAllByCreatedBy(Pageable pageable, Long userId);
    Page<Order> findByStaffId(Pageable pageable, Long staffId);
    Page<Order> findByCreatedAtBetween( Pageable pageable, LocalDateTime startDate, LocalDateTime endDate);
    Page<Order> findByCreatedAtBetweenAndStatus(Pageable pageable, LocalDateTime startDate, LocalDateTime endDate, String status);
    long countByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);
    long countByStatus(String status);
    long countByCreatedAtBetweenAndStatus(LocalDateTime startDate, LocalDateTime endDate, String status);
    @Query(value = "SELECT COALESCE (SUM(d.cost * d.quantity), 0) as total from orders o join order_details as d on o.id = d.order_id where o.created_at between ?1 and ?2",
            nativeQuery = true)
    long sumRevenue(LocalDateTime start, LocalDateTime end);
}
