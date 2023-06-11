package com.nix.managecafe.repository;

import com.nix.managecafe.model.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Objects;

public interface OrderRepo extends JpaRepository<Order, Long> {
    Page<Order> findByStatus(Pageable pageable, String status);

    Page<Order> findAllByCreatedBy(Pageable pageable, Long userId);

    Page<Order> findByStaffId(Pageable pageable, Long staffId);

    Page<Order> findByCreatedAtBetween(Pageable pageable, LocalDateTime startDate, LocalDateTime endDate);

    Page<Order> findByCreatedAtBetweenAndStatus(Pageable pageable, LocalDateTime startDate, LocalDateTime endDate, String status);

    @Query("SELECT o FROM Order o left join o.customer as c left join o.staff as s left join s.address left join c.address where (lower(concat(c.lastname,' ', c.firstname)) LIKE LOWER(CONCAT('%', :keyword, '%')) "
            + "or lower(concat(s.lastname,' ', s.firstname)) LIKE LOWER(CONCAT('%', :keyword, '%')) "
            + "or lower(s.username) like lower(CONCAT('%', :keyword, '%')) or lower(c.username) like lower(CONCAT('%', :keyword, '%'))) "
            + "and o.createdAt between :start and :end "
            + "and (:status IS NULL or o.status = :status)"
    )
    Page<Order> findByCreatedAtBetweenAndStatusAndKeyword(Pageable pageable, @Param("start") LocalDateTime startDate, @Param("end") LocalDateTime endDate, @Param("status") String status, @Param("keyword") String keyword);

    long countByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);

    long countByStatus(String status);

    long countByCreatedAtBetweenAndStatus(LocalDateTime startDate, LocalDateTime endDate, String status);

    @Query(value = "Select COALESCE(SUM(total), 0) FROM ( " +
            "            SELECT SUM(d.cost * d.quantity) + o.delivery_cost - o.amount_discount as total from orders o join order_details as d on o.id = d.order_id " +
            "            where o.created_at between ?1 and ?2 and o.status = 'PAID' " +
            "            group by o.id " +
            ") as temp;",
            nativeQuery = true)
    long sumRevenue(LocalDateTime start, LocalDateTime end);

    @Modifying
    @Query("update Order o set o.staff = null where o.staff.id = :id")
    void updateOrderByStaffIdWhenDelete(@Param("id") Long userId);

    @Query(value = "SELECT date, SUM(total) " +
            "FROM ( " +
            "    SELECT Date(o.created_at) as date, SUM(d.cost * d.quantity) - o.amount_discount + o.delivery_cost as total " +
            "    FROM orders o " +
            "    LEFT JOIN order_details d ON d.order_id = o.id " +
            "    Where o.created_at between :start and :end and o.status = 'PAID' " +
            "    GROUP BY d.order_id, o.id, Date(o.created_at) " +
            ") as temp " +
            "GROUP BY date", nativeQuery = true)
    List<Object[]> getRevenueRecent(@Param("start") LocalDateTime startDate, @Param("end") LocalDateTime endDate);

    @Query("select d.menu.name, SUM(d.cost * d.quantity) as total from Order o left join OrderDetail d on d.order.id = o.id where "
            + "o.createdAt between :start and :end "
            + "and o.status = 'PAID' "
            + "group by d.menu.id "
            + "order by total desc limit :top"
    )
    List<Object[]> getMenuTopSale(@Param("start") LocalDateTime startDate, @Param("end") LocalDateTime endDate, @Param("top") int top);

    @Query("select d.menu.name, SUM (d.quantity) as sl from Order o left join OrderDetail d on d.order.id = o.id where "
            + "o.createdAt between :start and :end "
            + "and o.status = 'PAID' "
            + "group by d.menu.id "
            + "order by sl desc limit :top"
    )
    List<Object[]> getMenuTopCount(@Param("start") LocalDateTime startDate, @Param("end") LocalDateTime endDate, @Param("top") int top);

    @Query(value = " select date ,SUM(total) from ( " +
            "select Date (o.created_at) as date, SUM(d.cost * d.quantity) + o.delivery_cost - o.amount_discount as total " +
            "from orders o  " +
            "left join order_details d on d.order_id = o.id " +
            "where YEAR(o.created_at) = :year " +
            "and month(o.created_at) = :month " +
            "and o.status = 'PAID' " +
            "group by Date(o.created_at), o.id " +
            "order by Date(o.created_at) " +
            ") as temp " +
            "group by date",
            nativeQuery = true
    )
    List<Object[]> getRevenueByMonthAndYear(@Param("month") int month, @Param("year") int year);

    @Query(value = " select month ,SUM(total) from ( " +
            "select MONTH (o.created_at) as month, SUM(d.cost * d.quantity) + o.delivery_cost - o.amount_discount as total " +
            "from orders o " +
            "left join order_details d on d.order_id = o.id " +
            "where YEAR(o.created_at) = :year " +
            "and o.status = 'PAID' " +
            "group by YEAR (o.created_at), MONTH(o.created_at), o.id " +
            "order by YEAR (o.created_at), MONTH(o.created_at) " +
            ") as temp " +
            "group by month",
            nativeQuery = true
    )
    List<Object[]> getRevenueByYear(@Param("year") int year);
    @Query(value = " select date ,SUM(total) from ( " +
            "select Date (o.created_at) as date, SUM(d.cost * d.quantity) + o.delivery_cost - o.amount_discount as total " +
            "from orders o  " +
            "left join order_details d on d.order_id = o.id " +
            "where o.created_at between :start and :end " +
            "and o.status = 'PAID' " +
            "group by Date(o.created_at), o.id " +
            "order by Date(o.created_at) " +
            ") as temp " +
            "group by date",
            nativeQuery = true
    )
    List<Object[]> getRevenueBetween(@Param("start") LocalDateTime startDate, @Param("end") LocalDateTime endDate);
    @Query(value = "select d.menu.name, SUM (d.quantity) as sl, SUM(d.quantity * d.cost) as revenue from Order o left join OrderDetail d on d.order.id = o.id where " +
            "o.createdAt between :start and :end " +
            "and o.status = 'PAID' " +
            "group by d.menu.id " +
            "order by revenue desc, sl desc limit :top")
    List<Object[]> getTopMenu(@Param("start") LocalDateTime startDate, @Param("end") LocalDateTime endDate, @Param("top") int top);
}
