package vn.com.twendie.avis.mobile.api.controller;

import com.google.common.collect.ImmutableMap;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpRequest;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import vn.com.twendie.avis.api.core.util.DataUtil;
import vn.com.twendie.avis.api.core.util.ListUtils;
import vn.com.twendie.avis.api.rest.model.ApiResponse;
import vn.com.twendie.avis.api.rest.model.GeneralPageResponse;
import vn.com.twendie.avis.data.enumtype.NotificationContentEnum;
import vn.com.twendie.avis.data.enumtype.NotificationSettingTypeEnum;
import vn.com.twendie.avis.data.model.Contract;
import vn.com.twendie.avis.data.model.Notification;
import vn.com.twendie.avis.data.model.SystemLog;
import vn.com.twendie.avis.data.model.User;
import vn.com.twendie.avis.mobile.api.adapter.NotificationDTOAdapter;
import vn.com.twendie.avis.mobile.api.constant.MobileConstant;
import vn.com.twendie.avis.mobile.api.model.payload.LogsPayload;
import vn.com.twendie.avis.mobile.api.model.payload.NotificationPayload;
import vn.com.twendie.avis.mobile.api.model.payload.UpdateNotificationStatusPayload;
import vn.com.twendie.avis.mobile.api.model.response.NotificationDTO;
import vn.com.twendie.avis.mobile.api.service.ContractService;
import vn.com.twendie.avis.mobile.api.service.SystemLogService;
import vn.com.twendie.avis.mobile.api.service.UserService;
import vn.com.twendie.avis.mobile.api.task.SendNotificationTask;
import vn.com.twendie.avis.notification.model.onesignal.PushNotiPayload;
import vn.com.twendie.avis.notification.model.onesignal.PushNotiResponse;
import vn.com.twendie.avis.notification.service.NotificationService;
import vn.com.twendie.avis.notification.service.PushNotificationService;
import vn.com.twendie.avis.security.core.annotation.CurrentUser;
import vn.com.twendie.avis.security.core.model.UserPrincipal;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.Collections;

@Slf4j
@RestController
@RequestMapping("/notifications")
public class NotificationController {

    private final UserService userService;
    private final ContractService contractService;
    private final NotificationService notificationService;
    private final PushNotificationService pushNotificationService;
    private final ListUtils listUtils;
    private final SendNotificationTask sendNotificationTask;
    private final ModelMapper modelMapper;
    private final SystemLogService systemLogService;

    public NotificationController(UserService userService,
                                  ContractService contractService,
                                  NotificationService notificationService,
                                  PushNotificationService pushNotificationService,
                                  ListUtils listUtils,
                                  SendNotificationTask sendNotificationTask,
                                  ModelMapper modelMapper, SystemLogService systemLogService) {
        this.userService = userService;
        this.contractService = contractService;
        this.notificationService = notificationService;
        this.pushNotificationService = pushNotificationService;
        this.listUtils = listUtils;
        this.sendNotificationTask = sendNotificationTask;
        this.modelMapper = modelMapper;
        this.systemLogService = systemLogService;
    }

    @PostMapping("test")
    @ResponseBody
    public ApiResponse<?> test(@CurrentUser UserDetails userDetails){
//        User user = ((UserPrincipal) userDetails).getUser();
//        Contract contract = contractService.findById(user.getCurrentContractId());
//        NotificationContentEnum contentEnum = NotificationContentEnum.END_JOURNEY_DIARY_SIGNATURE_WEEK;
//        Notification notification = notificationService.build(user.getId(), contract, contentEnum, NotificationTypeEnum.SIGNATURE, "1");
//        notificationService.save(notification);
//        PushNotiResponse pushNotiResponse = null;
//        PushNotiPayload pushNotiPayload = pushNotificationService.build(notification);
//        try{
//            pushNotiResponse = pushNotificationService.push(pushNotiPayload);
//        }catch (Exception ignored){
//        }
//
//        return ApiResponse.success(ImmutableMap.of(
//                "player_ids", pushNotiPayload.getIncludePlayerIds(),
//                "response", ObjectUtils.defaultIfNull(pushNotiResponse, Collections.EMPTY_MAP)
//        ));
        return null;
    }


    @PostMapping("/create")
    @ResponseBody
    public ApiResponse<?> createNotification(@RequestBody NotificationPayload payload) {

        User user = userService.getUserByUsername(payload.getUsername());
        Contract contract = contractService.findById(payload.getData().getSpecId());
        NotificationContentEnum notificationContentEnum = NotificationContentEnum.findById(payload.getNotificationContentId());
        Notification notification = notificationService
                .build(user.getId(), contract, notificationContentEnum);
        notificationService.save(notification);
        PushNotiResponse pushNotiResponse = null;
        PushNotiPayload pushNotiPayload = pushNotificationService.build(notification);
        try {
            pushNotiResponse = pushNotificationService.push(pushNotiPayload);
        } catch (Exception ignored) {
        }
        return ApiResponse.success(ImmutableMap.of(
                "player_ids", pushNotiPayload.getIncludePlayerIds(),
                "response", ObjectUtils.defaultIfNull(pushNotiResponse, Collections.EMPTY_MAP)
        ));
    }

    @GetMapping("")
    public ApiResponse<?> getNotifications(
            @RequestParam(required = false, defaultValue = MobileConstant.DEFAULT_STARTER_PAGE) int page,
            @CurrentUser UserDetails userDetails) {
        User user = ((UserPrincipal) userDetails).getUser();
        Page<Notification> notifications = notificationService.findAllByDriverId(user.getId(), page);
        Page<NotificationDTO> notificationDTOS = listUtils.transform(notifications, new NotificationDTOAdapter());
        return ApiResponse.success(GeneralPageResponse.toResponse(notificationDTOS));
    }

    @PutMapping("/update")
    @Transactional
    public ApiResponse<?> updateNotificationStatus(@Valid @RequestBody UpdateNotificationStatusPayload payload) {
        notificationService.updateNotificationStatus(payload.getNotificationId(), payload.getStatus());
        return ApiResponse.success(Collections.EMPTY_MAP);
    }

    @PostMapping("test/push")
    public void testPush(@RequestParam("type") NotificationSettingTypeEnum type){
        sendNotificationTask.schedulePushNotification(type);
    }

    @PostMapping("write-logs-client")
    public ApiResponse<?> writeLogs(@RequestBody LogsPayload logsPayload,
                                    @CurrentUser UserDetails userDetails,
                                    HttpServletRequest httpServletRequest) {
        try {

            if (StringUtils.isEmpty(logsPayload.getDevice()))
                logsPayload.setDevice("MOBILE");
            if (StringUtils.isEmpty(logsPayload.getIp()))
                logsPayload.setIp(DataUtil.getClientIpAddr(httpServletRequest));

            SystemLog systemLog = modelMapper.map(logsPayload, SystemLog.class);

            systemLog.setType("CLIENT");
            if (userDetails != null) {
                User user = ((UserPrincipal) userDetails).getUser();
                if (user != null) {
                    systemLog.setUserId(user.getId());
                    if (StringUtils.isEmpty(systemLog.getUser())) {
                        systemLog.setUser(user.getUsername());
                    }
                }
            }
            systemLogService.save(systemLog);
            return ApiResponse.success("Successfully");
        } catch (Exception e) {
            log.error(e.getMessage());
            e.printStackTrace();
            return ApiResponse.error(e.getMessage());
        }
    }
}
