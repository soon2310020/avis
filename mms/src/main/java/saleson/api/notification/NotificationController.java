package saleson.api.notification;

import com.emoldino.api.common.resource.base.accesscontrol.annotation.DataLeakDetector;
import com.emoldino.api.common.resource.base.alert.util.AlertUtils;
import com.emoldino.framework.util.BeanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import saleson.api.notification.payload.NotificationPayload;
import saleson.api.user.UserRepository;
import saleson.api.user.UserService;
import saleson.common.enumeration.AlertType;
import saleson.common.payload.ApiResponse;
import saleson.common.payload.ApiResponseEntity;
import saleson.common.util.DataUtils;
import saleson.common.util.SecurityUtils;
import saleson.dto.RestData;
import saleson.model.Notification;
import saleson.model.User;
import saleson.service.notification.BroadCastServiceImpl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/notification")
public class NotificationController {
    @Autowired
    NotificationService notificationService;
    @Autowired
    NotificationRepository notificationRepository;
    @Autowired
    UserService userService;


    @GetMapping("/list-of-user")
    public ResponseEntity getNotificationOfUser(
//            NotificationPayload payload,
            @RequestParam(value = "pageIndex", required = false) Integer pageIndex,
            @RequestParam(value = "pageSize", required = false) Integer pageSize) {
//        NotificationPayload payload = new NotificationPayload();
//        payload.setIsRead(false);
//        List<Sort.Order> sorts = Arrays.asList(
//                Sort.Order.asc("isRead"),
//                Sort.Order.desc("notificationCreatedAt")
//        );
//        Pageable pageable = PageRequest.of(0, 1, Sort.by(sorts));
//
//        Page<Notification> pageContent = notificationService.findAll(payload.getPredicate(), DataUtils.getPageable(pageIndex, pageSize, pageable));
//
////        List<PartDTO> partDTOS = pageContent.getContent().stream().map(t -> PartDTO.convertToDTO(t)).collect(Collectors.toList());
//        return ApiResponseEntity.ok(new RestData(pageContent.getTotalElements(), pageContent.getContent()));
        return ApiResponseEntity.ok(new RestData());
    }

    @GetMapping("/number-unread-of-user")
    public Long numberUnreadOfUser() {
        NotificationPayload payload = new NotificationPayload();
        payload.setIsRead(false);
        Page<Notification> pageContent = notificationService.findAll(payload.getPredicate(), PageRequest.of(0, 1));
        return pageContent.getTotalElements();
    }

    @PostMapping("/clear/{id}")
    public ApiResponse clearNotification(@PathVariable("id") Long id) {
        Notification notification = notificationRepository.findById(id).orElse(null);
        if (notification == null) {
            return new ApiResponse(false, "Fail!");
        }
        notification.setRead(true);
        notificationRepository.save(notification);
        return new ApiResponse(true, "OK!");
    }

    @PostMapping("/clear-all")
    public ApiResponse clearAllNotification() {
        notificationService.clearAll();
        return new ApiResponse(true, "OK!");
    }

    @PostMapping("/change-status-notification-system-node")
    public ApiResponse changeNotificationSystemNode(@RequestParam(value = "disable", required = true) boolean disable){
        User user = userService.findById(SecurityUtils.getUserId());
        if(user==null){
            return new ApiResponse(false, "Fail!");
        }
        user.setDisableNotificationSystemNode(disable);
        userService.save(user);
        return new ApiResponse(true, "OK!");

    }

    @GetMapping("/test-user-alert")
    public ApiResponse testUserAlert(@RequestParam(name = "type") String type,
                                     @RequestParam(name = "id") Long id) {
        if ("TOOLING".equals(type)) {
            List<Long> userIds = AlertUtils.getUserIdsByMold(AlertType.DISCONNECTED, id);
            return ApiResponse.success("OK", BeanUtils.get(UserRepository.class).findAllById(userIds));
        } else {
            return ApiResponse.success("OK", BeanUtils.get(UserRepository.class).findAllById(AlertUtils.getUserIdsByTerminal(AlertType.TERMINAL_DISCONNECTED, id)));
        }
    }

}
