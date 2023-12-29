package vn.com.twendie.avis.api.controller;

import org.springframework.data.domain.Page;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import vn.com.twendie.avis.api.core.util.ListUtils;
import vn.com.twendie.avis.api.model.response.NotificationDTO;
import vn.com.twendie.avis.api.rest.model.ApiResponse;
import vn.com.twendie.avis.data.model.Notification;
import vn.com.twendie.avis.data.model.User;
import vn.com.twendie.avis.notification.service.NotificationService;
import vn.com.twendie.avis.security.annotation.RequirePermission;
import vn.com.twendie.avis.security.core.annotation.CurrentUser;
import vn.com.twendie.avis.security.core.model.UserPrincipal;

import javax.validation.constraints.Positive;

@RestController
@RequestMapping("/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    private final ListUtils listUtils;

    public NotificationController(NotificationService notificationService,
                                  ListUtils listUtils) {
        this.notificationService = notificationService;
        this.listUtils = listUtils;
    }

    @GetMapping
    @RequirePermission(acceptedRoles = "Customer")
    public ApiResponse<?> getNotifications(@Positive @RequestParam(value = "page", defaultValue = "1") int page,
                                           @Positive @RequestParam(value = "size", defaultValue = "10") int size,
                                           @CurrentUser UserDetails userDetails) {
        User user = ((UserPrincipal) userDetails).getUser();
        Page<Notification> notifications = notificationService.findByUserId(user.getId(), page, size);
        Page<NotificationDTO> notificationDTOs = listUtils.mapAll(notifications, NotificationDTO.class);
        return ApiResponse.success(notificationDTOs);
    }

}
