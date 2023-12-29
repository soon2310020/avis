package saleson.api.broadcastNotification;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import saleson.api.broadcastNotification.payload.BroadcastNotificationPayload;
import saleson.api.user.UserRepository;
import saleson.common.payload.ApiResponse;
import saleson.common.payload.ApiResponseEntity;
import saleson.common.util.DataUtils;
import saleson.dto.BroadcastNotificationDTO;
import saleson.dto.RestData;
import saleson.model.BroadcastNotification;
import saleson.model.User;
import saleson.service.notification.BroadCastServiceImpl;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/api/broadcast-notification")
public class BroadcastNotificationController {
    @Autowired
    UserRepository userRepository;
    @Autowired
    BroadCastServiceImpl broadCastService;
    @Autowired
    BroadcastNotificationService broadcastNotificationService;
    @Autowired
    BroadcastNotificationRepository broadcastNotificationRepository;

    @GetMapping("/list-of-user")
    public ResponseEntity getNotificationOfUser(
            BroadcastNotificationPayload payload,
            @RequestParam(value = "pageIndex", required = false) Integer pageIndex,
            @RequestParam(value = "pageSize", required = false) Integer pageSize) {
        List<Sort.Order> sorts = Arrays.asList(
//                Sort.Order.asc("isRead"),
                Sort.Order.desc("createdAt")
        );
        Pageable pageable = PageRequest.of(0, 1, Sort.by(sorts));

        Page<BroadcastNotification> pageContent = broadcastNotificationService.findAll(payload.getPredicate(), DataUtils.getPageable(pageIndex, pageSize, pageable));

        List<BroadcastNotificationDTO> dtos =broadcastNotificationService.convertToDTO(pageContent.getContent());
        return ApiResponseEntity.ok(new RestData(pageContent.getTotalElements(), dtos));
    }

    @PostMapping("/read/{id}")
    public ApiResponse clearNotification(@PathVariable("id") Long id,
                                         @RequestParam(value = "read", required = false) Boolean read) {
        BroadcastNotification notification = broadcastNotificationRepository.findById(id).orElse(null);
        if (notification == null) {
            return new ApiResponse(false, "Fail!");
        }
        if (read != null)
            notification.setRead(read);
        else
            notification.setRead(true);
        if(read==null || read)
            notification.setSeenTime(Instant.now());

        notification.setUpdatedAt(Instant.now());
        broadcastNotificationRepository.save(notification);
        return new ApiResponse(true, "OK!");
    }
    @DeleteMapping("{id}")
    public ApiResponse deleteNotification(@PathVariable("id") Long id) {
        BroadcastNotification notification = broadcastNotificationRepository.findById(id).orElse(null);
        if (notification == null) {
            return new ApiResponse(false, "Notification not exist!");
        }
        broadcastNotificationRepository.delete(notification);
        return new ApiResponse(true, "OK!");
    }

    @PostMapping("/read-all")
    public ApiResponse clearAllNotification() {
        broadcastNotificationService.clearAll();
        return new ApiResponse(true, "OK!");
    }


    @PostMapping(value = "notiToNativeApp")
    public ResponseEntity notiToNativeApp(@RequestParam( required = false) String message,
                                          @RequestParam(value = "userId", required = false) Long userId){
        if(StringUtils.isEmpty(message)) message = "New version is available now!\n" +
                "When you face social login problem via your Google account, please upgrade the application";

        List<User> allUsers= new ArrayList<>();
        if(userId!=null){
            User user= userRepository.findById(userId).orElse(null);
            if(user!=null) allUsers.add(user);
        }else {
            allUsers= userRepository.findAllByEnabledIsTrueAndDeletedIsFalse();
        }

        for (User u:allUsers) {
            broadCastService.sendNotificationToUser(u.getId(), message, null);
        }
        return ResponseEntity.ok(HttpStatus.OK);
    }

}
