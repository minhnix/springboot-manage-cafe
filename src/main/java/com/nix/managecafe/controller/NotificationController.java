package com.nix.managecafe.controller;

import com.nix.managecafe.model.Notification;
import com.nix.managecafe.payload.response.PagedResponse;
import com.nix.managecafe.service.NotificationService;
import com.nix.managecafe.util.AppConstants;
import org.aspectj.weaver.ast.Not;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

@RestController
public class NotificationController {
    private final NotificationService notificationService;

    private final SimpMessagingTemplate simpMessagingTemplate;

    public NotificationController(NotificationService notificationService, SimpMessagingTemplate simpMessagingTemplate) {
        this.notificationService = notificationService;
        this.simpMessagingTemplate = simpMessagingTemplate;
    }

    @GetMapping("/api/v1/users/{id}/notification")
    public PagedResponse<Notification> getNotification(
            @PathVariable("id") Long id,
            @RequestParam(value = "page", defaultValue = AppConstants.DEFAULT_PAGE_NUMBER) int page,
            @RequestParam(value = "size", defaultValue = AppConstants.DEFAULT_PAGE_SIZE) int size,
            @RequestParam(value = "read", defaultValue = "False") boolean read
    ) {
        if (read) {
            return notificationService.getNotificationsByToUser(page, size, id);
        } else {
            return notificationService.getNotificationsByToUserNotRead(page, size, id);
        }
    }

    @PostMapping("/api/v1/notification")
    public Notification pushNotification(@RequestBody Notification notification) {
        return notificationService.createNotification(notification);
    }

    @PutMapping("/api/v1/notification")
    public void changeAllToRead(@RequestParam("userId") Long userId) {
        notificationService.changeAllToRead(userId);
    }
    @PutMapping("/api/v1/notification/{id}")
    public void changeNotificationToRead(@PathVariable("id") Long id) {
        notificationService.changeToRead(id);
    }

    //user send to system
    //subscribe /system/notification
    //send /app/system-notification
    @MessageMapping("/system-notification")
    @SendTo("/system/notification")
    public Notification send(@Payload Notification notification) throws Exception {
        return notification;
    }

    //system send to specific user
    //subscribe /user/David/notification
    //send app/user-notification
    @MessageMapping("/user-notification")
    public void sendToSpecificUser(@Payload Notification notification) {
        simpMessagingTemplate.convertAndSendToUser(notification.getToUser().getId().toString(), "/notification", notification);
    }
}
