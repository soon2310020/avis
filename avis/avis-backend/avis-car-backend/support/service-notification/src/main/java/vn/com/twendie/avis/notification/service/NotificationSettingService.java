package vn.com.twendie.avis.notification.service;

import vn.com.twendie.avis.data.model.NotificationSetting;
import vn.com.twendie.avis.data.model.User;
import vn.com.twendie.avis.notification.model.onesignal.NotificationSettingPayload;

import java.util.List;

public interface NotificationSettingService {

    void changeSetting(User user, NotificationSettingPayload notificationSettingPayload);

    NotificationSetting createIfNotExists(User user);

    List<NotificationSetting> findByUserIdIn(List<Long> userIds);

    NotificationSetting findByUserId(Long userId);

    NotificationSetting save(NotificationSetting notificationSetting);
}
