package vn.com.twendie.avis.notification.service;

import vn.com.twendie.avis.data.model.Notification;
import vn.com.twendie.avis.notification.model.onesignal.PushNotiPayload;
import vn.com.twendie.avis.notification.model.onesignal.PushNotiResponse;

public interface PushNotificationService {

    PushNotiPayload build(Notification notification);

    void push(Long notificationId) throws Exception;

    PushNotiResponse push(PushNotiPayload pushNotiPayload) throws Exception;

    default PushNotiResponse buildAndPush(Notification notification) throws Exception {
        return push(build(notification));
    }

}
