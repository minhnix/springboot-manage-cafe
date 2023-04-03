package com.nix.managecafe.repository;

import com.nix.managecafe.model.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepo extends JpaRepository<Notification, Long> {
    Page<Notification> findAllByToUserIdAndWatchedIsFalseOrderByCreatedAtDesc(Pageable pageable, Long userId);
    Page<Notification> findAllByToUserIdOrderByCreatedAtDesc(Pageable pageable, Long userId);
    List<Notification> findAllByToUserIdAndWatchedIsFalse(Long userId);
}
