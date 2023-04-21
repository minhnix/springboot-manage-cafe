package com.nix.managecafe.repository;

import com.nix.managecafe.model.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface NotificationRepo extends JpaRepository<Notification, Long> {
    Page<Notification> findAllByToUserIdAndWatchedIsFalseOrderByCreatedAtDesc(Pageable pageable, Long userId);
    Page<Notification> findAllByToUserIdOrderByCreatedAtDesc(Pageable pageable, Long userId);
    List<Notification> findAllByToUserIdAndWatchedIsFalse(Long userId);
    @Query("select count (n.id) from Notification n where n.watched = FALSE and n.toUser.id = :userId")
    Long countUnread(@Param("userId") Long userId);
}
