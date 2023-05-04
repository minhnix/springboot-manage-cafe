package com.nix.managecafe.repository;

import com.nix.managecafe.model.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;

public interface OrderRepo extends JpaRepository<Order, Long> {
    Page<Order> findAllByStatus(Pageable pageable, String status);
    Page<Order> findAllByCreatedBy(Pageable pageable, Long userId);
    Page<Order> findByStaffId(Pageable pageable, Long staffId);
    Page<Order> findByCreatedAtBetween( Pageable pageable, LocalDateTime startDate, LocalDateTime endDate);
    Page<Order> findByCreatedAtBetweenAndStatus(Pageable pageable, LocalDateTime startDate, LocalDateTime endDate, String status);
    @Query("SELECT o FROM Order o left join o.customer as c left join o.staff as s left join s.address left join c.address where (lower(concat(c.lastname,' ', c.firstname)) LIKE LOWER(CONCAT('%', :keyword, '%')) "
            + "or lower(concat(s.lastname,' ', s.firstname)) LIKE LOWER(CONCAT('%', :keyword, '%')) "
            + "or lower(s.username) like lower(CONCAT('%', :keyword, '%')) or lower(c.username) like lower(CONCAT('%', :keyword, '%'))) "
            + "and o.createdAt between :start and :end "
            + "and (:status IS NULL or o.status = :status)"
    )
    Page<Order> findByCreatedAtBetweenAndStatusAndKeyword(Pageable pageable, @Param("start") LocalDateTime startDate,@Param("end") LocalDateTime endDate,@Param("status") String status , @Param("keyword") String keyword);

    long countByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);
    long countByStatus(String status);
    long countByCreatedAtBetweenAndStatus(LocalDateTime startDate, LocalDateTime endDate, String status);
    @Query(value = "SELECT COALESCE (SUM(d.cost * d.quantity), 0) as total from orders o join order_details as d on o.id = d.order_id where o.created_at between ?1 and ?2 and o.status = 'PAID' ",
            nativeQuery = true)
    long sumRevenue(LocalDateTime start, LocalDateTime end);

    @Modifying
    @Query("update Order o set o.staff = null where o.staff.id = :id")
    void updateOrderByStaffIdWhenDelete(@Param("id") Long userId);
}
