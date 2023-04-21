package com.nix.managecafe.controller;

import com.nix.managecafe.exception.AuthenticationException;
import com.nix.managecafe.model.Notification;
import com.nix.managecafe.model.enumname.RoleName;
import com.nix.managecafe.payload.response.PagedResponse;
import com.nix.managecafe.security.CurrentUser;
import com.nix.managecafe.security.UserPrincipal;
import com.nix.managecafe.service.NotificationService;
import com.nix.managecafe.util.AppConstants;
import org.aspectj.weaver.ast.Not;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.web.bind.annotation.*;

@RestController
// TODO: Add current user
public class NotificationController {
    private final NotificationService notificationService;

    private final SimpMessagingTemplate simpMessagingTemplate;

    public NotificationController(NotificationService notificationService, SimpMessagingTemplate simpMessagingTemplate) {
        this.notificationService = notificationService;
        this.simpMessagingTemplate = simpMessagingTemplate;
    }

    @GetMapping("/api/v1/users/unread-notification")
    public Long getAmountUnreadNotification(@CurrentUser UserPrincipal userPrincipal
    ) {
        if (userPrincipal == null) throw new AuthenticationException("Full authentication to get resource");
        if (userPrincipal.getAuthorities().contains(new SimpleGrantedAuthority(RoleName.ROLE_STAFF.name()))) {
            return notificationService.getAmountUnreadNotification(1L);
        }
            return notificationService.getAmountUnreadNotification(userPrincipal.getId());
    }

    @GetMapping("/api/v1/users/notification")
    public PagedResponse<Notification> getNotification(
            @RequestParam(value = "page", defaultValue = AppConstants.DEFAULT_PAGE_NUMBER) int page,
            @RequestParam(value = "size", defaultValue = AppConstants.DEFAULT_PAGE_SIZE) int size,
            @RequestParam(value = "read", defaultValue = "False") boolean read,
            @CurrentUser UserPrincipal userPrincipal
            ) {
        if (userPrincipal == null) throw new AuthenticationException("Full authentication to get resource");
        if (userPrincipal.getAuthorities().contains(new SimpleGrantedAuthority(RoleName.ROLE_STAFF.name()))) {
            return notificationService.getNotificationsByToUser(page, size, 1L);
        }
        if (!read) {
            return notificationService.getNotificationsByToUser(page, size, userPrincipal.getId());
        } else {
            return notificationService.getNotificationsByToUserNotRead(page, size, userPrincipal.getId());
        }
    }

    @PostMapping("/api/v1/notification")
    public Notification pushNotification(@RequestBody Notification notification, @CurrentUser UserPrincipal userPrincipal) {
        if (userPrincipal == null) throw new AuthenticationException("Full authentication to get resource");
        return notificationService.createNotification(notification);
    }

    @PutMapping("/api/v1/notification")
    public void changeAllToRead(@CurrentUser UserPrincipal userPrincipal) {
        if (userPrincipal == null) throw new AuthenticationException("Full authentication to get resource");
        notificationService.changeAllToRead(userPrincipal.getId());
    }
    @PutMapping("/api/v1/notification/{id}")
    public void changeNotificationToRead(@PathVariable("id") Long id, @CurrentUser UserPrincipal userPrincipal) {
        if (userPrincipal == null) throw new AuthenticationException("Full authentication to get resource");
        notificationService.changeToRead(id, userPrincipal);
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
