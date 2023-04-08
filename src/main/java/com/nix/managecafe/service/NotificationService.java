package com.nix.managecafe.service;

import com.nix.managecafe.exception.ForbiddenException;
import com.nix.managecafe.exception.ResourceNotFoundException;
import com.nix.managecafe.model.Notification;
import com.nix.managecafe.model.Order;
import com.nix.managecafe.payload.response.PagedResponse;
import com.nix.managecafe.repository.NotificationRepo;
import com.nix.managecafe.security.UserPrincipal;
import com.nix.managecafe.util.ValidatePageable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
public class NotificationService {
    private final NotificationRepo notificationRepo;

    public NotificationService(NotificationRepo notificationRepo) {
        this.notificationRepo = notificationRepo;
    }

    public Notification createNotification(Notification notification) {
        return notificationRepo.save(notification);
    }


    public Notification getOne(Long id) {
        return notificationRepo.findById(id).orElseThrow(() -> new ResourceNotFoundException("Notification", "id", id));
    }

    public PagedResponse<Notification> getNotificationsByToUserNotRead(int page, int size, Long userId) {
        ValidatePageable.invoke(page, size);

        Pageable pageable = PageRequest.of(page, size);
        Page<Notification> notifications = notificationRepo.findAllByToUserIdAndWatchedIsFalseOrderByCreatedAtDesc(pageable, userId);

        return new PagedResponse<>(notifications.getContent(), notifications.getNumber(),
                notifications.getSize(), notifications.getTotalElements(), notifications.getTotalPages(), notifications.isLast());
    }

    public PagedResponse<Notification> getNotificationsByToUser(int page, int size, Long userId) {
        ValidatePageable.invoke(page, size);

        Pageable pageable = PageRequest.of(page, size);
        Page<Notification> notifications = notificationRepo.findAllByToUserIdOrderByCreatedAtDesc(pageable, userId);

        return new PagedResponse<>(notifications.getContent(), notifications.getNumber(),
                notifications.getSize(), notifications.getTotalElements(), notifications.getTotalPages(), notifications.isLast());
    }

    public Notification changeToRead(Long id, UserPrincipal userPrincipal) {
        Notification notification = notificationRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Notification", "id", id));
        if (!Objects.equals(notification.getCreatedBy(), userPrincipal.getId())) {
            throw new ForbiddenException("Access Denied");
        }
        notification.setWatched(true);
        return notificationRepo.save(notification);
    }

    public void changeAllToRead(Long userId) {
        List<Notification> notification = notificationRepo.findAllByToUserIdAndWatchedIsFalse(userId);
        notification.forEach(
                notification1 -> {
                    notification1.setWatched(true);
                    notificationRepo.save(notification1);
                });
    }

    public void clear() {
        notificationRepo.deleteAll();
    }
}
