package vn.com.twendie.avis.notification.service.impl;

import org.springframework.stereotype.Service;
import vn.com.twendie.avis.api.rest.exception.BadRequestException;
import vn.com.twendie.avis.data.model.NotificationSetting;
import vn.com.twendie.avis.data.model.User;
import vn.com.twendie.avis.notification.model.onesignal.NotificationSettingPayload;
import vn.com.twendie.avis.notification.repository.NotificationSettingRepo;
import vn.com.twendie.avis.notification.service.NotificationSettingService;

import java.util.List;

@Service
public class NotificationSettingServiceImpl implements NotificationSettingService {

    private NotificationSettingRepo notificationSettingRepo;

    public NotificationSettingServiceImpl(NotificationSettingRepo notificationSettingRepo){
        this.notificationSettingRepo = notificationSettingRepo;
    }

    @Override
    public void changeSetting(User user, NotificationSettingPayload notificationSettingPayload) {
        if(user == null) throw new BadRequestException("user not found");
        NotificationSetting notificationSetting = notificationSettingRepo.findByUser(user.getId());
        if(notificationSetting == null) throw new BadRequestException("notification not found");
        notificationSetting.setDay(notificationSettingPayload.isDay());
        notificationSetting.setWeek(notificationSettingPayload.isWeek());
        notificationSetting.setMonth(notificationSettingPayload.isMonth());
        notificationSettingRepo.save(notificationSetting);

    }

    @Override
    public NotificationSetting createIfNotExists(User user) {
        NotificationSetting notificationSetting = notificationSettingRepo.findByUser(user.getId());
        if(notificationSetting == null){
            notificationSetting = new NotificationSetting();
            notificationSetting.setUser(user);
            notificationSetting.setMonth(false);
            notificationSetting.setWeek(false);
            notificationSetting.setDay(true);
            notificationSettingRepo.save(notificationSetting);
        }
        return notificationSetting;
    }

    @Override
    public List<NotificationSetting> findByUserIdIn(List<Long> userIds) {
        return notificationSettingRepo.findByUserIdIn(userIds);
    }

    @Override
    public NotificationSetting findByUserId(Long userId) {
        return notificationSettingRepo.findByUser(userId);
    }

    @Override
    public NotificationSetting save(NotificationSetting notificationSetting) {
        return notificationSettingRepo.save(notificationSetting);
    }
}
