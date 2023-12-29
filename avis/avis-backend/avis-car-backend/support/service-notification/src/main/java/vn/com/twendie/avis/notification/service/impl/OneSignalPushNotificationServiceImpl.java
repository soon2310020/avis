package vn.com.twendie.avis.notification.service.impl;

import lombok.extern.slf4j.Slf4j;
import okhttp3.Request;
import okhttp3.Response;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import vn.com.twendie.avis.api.core.util.OkHttpUtils;
import vn.com.twendie.avis.data.model.Notification;
import vn.com.twendie.avis.data.service.TokenFirebaseService;
import vn.com.twendie.avis.notification.config.OneSignalConfig;
import vn.com.twendie.avis.notification.model.onesignal.CommonData;
import vn.com.twendie.avis.notification.model.onesignal.NotiContent;
import vn.com.twendie.avis.notification.model.onesignal.PushNotiPayload;
import vn.com.twendie.avis.notification.model.onesignal.PushNotiResponse;
import vn.com.twendie.avis.notification.service.NotificationService;
import vn.com.twendie.avis.notification.service.PushNotificationService;

import java.util.Objects;
import java.util.Set;

import static vn.com.twendie.avis.data.enumtype.NotificationStatusEnum.FAILED;
import static vn.com.twendie.avis.data.enumtype.NotificationStatusEnum.SUCCESS;
import static vn.com.twendie.avis.queue.constant.QueueConstant.RoutingKeys.PUSH_NOTIFICATION;

@Service
@Slf4j
public class OneSignalPushNotificationServiceImpl implements PushNotificationService {

    private final OneSignalConfig oneSignalConfig;
    private final NotificationService notificationService;
    private final TokenFirebaseService tokenFirebaseService;
    private final OkHttpUtils okHttpUtils;

    public OneSignalPushNotificationServiceImpl(OneSignalConfig oneSignalConfig,
                                                @Lazy NotificationService notificationService,
                                                TokenFirebaseService tokenFirebaseService,
                                                OkHttpUtils okHttpUtils) {
        this.oneSignalConfig = oneSignalConfig;
        this.notificationService = notificationService;
        this.tokenFirebaseService = tokenFirebaseService;
        this.okHttpUtils = okHttpUtils;
    }

    @Override
    public PushNotiPayload build(Notification notification) {
        Set<String> playerIds = tokenFirebaseService.findAllValidTokenByUSerId(notification.getUserId());
        String content = notification.getNotificationContent().getContent();
        try {
            Object[] params = StringUtils.defaultString(notification.getParams()).split(",");
            content = String.format(content, params);
        } catch (Exception ignored) {
        }
        return PushNotiPayload.builder()
                .contents(NotiContent.builder().en(content).build())
                .includePlayerIds(playerIds)
                .data(CommonData.builder()
                        .notificationId(notification.getId())
                        .type(notification.getType())
                        .specId(notification.getSpecId())
                        .build())
                .build();
    }

    @Override
    @RabbitListener(queues = PUSH_NOTIFICATION, concurrency = "5-10")
    public void push(Long notificationId) throws InterruptedException {
        Thread.sleep(10000);
        Notification notification = null;
        try {
            notification = notificationService.findById(notificationId);
            PushNotiResponse response = buildAndPush(notification);
            if (Objects.nonNull(response.getId())) {
                notification.setStatus(SUCCESS.value());
            } else {
                notification.setStatus(FAILED.value());
            }
            notificationService.save(notification);
        } catch (Exception e) {
            log.error("Error push notification with id {}: {}", notificationId, ExceptionUtils.getRootCauseMessage(e));
            if (Objects.nonNull(notification)) {
                notification.setStatus(FAILED.value());
                notificationService.save(notification);
            }
        }
    }

    @Override
    public PushNotiResponse push(PushNotiPayload pushNotiPayload) throws Exception {
        if (pushNotiPayload.getIncludePlayerIds().isEmpty()) {
            return new PushNotiResponse("", 0);
        } else {
            pushNotiPayload.setAppId(oneSignalConfig.getAppId());
            Request request = okHttpUtils.createRequestBuilder()
                    .url(oneSignalConfig.getUrl() + "/api/v1/notifications")
                    .addHeader(HttpHeaders.AUTHORIZATION, "Basic " + oneSignalConfig.getApiKey())
                    .post(okHttpUtils.createPostBody(pushNotiPayload))
                    .build();
            Response response = okHttpUtils.execute(request);
            return okHttpUtils.getResponseAsObject(response, PushNotiResponse.class);
        }
    }

}
