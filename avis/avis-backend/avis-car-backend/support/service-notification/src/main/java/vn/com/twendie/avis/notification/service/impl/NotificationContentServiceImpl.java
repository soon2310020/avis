package vn.com.twendie.avis.notification.service.impl;

import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import vn.com.twendie.avis.api.rest.exception.NotFoundException;
import vn.com.twendie.avis.data.model.NotificationContent;
import vn.com.twendie.avis.notification.repository.NotificationContentRepo;
import vn.com.twendie.avis.notification.service.NotificationContentService;

@Service
@CacheConfig(cacheNames = "NotificationContent")
public class NotificationContentServiceImpl implements NotificationContentService {

    private final NotificationContentRepo notificationContentRepo;

    public NotificationContentServiceImpl(NotificationContentRepo notificationContentRepo) {
        this.notificationContentRepo = notificationContentRepo;
    }

    @Override
    @Cacheable(key = "#id")
    public NotificationContent findById(long id) {
        return notificationContentRepo.findById(id).orElseThrow(() ->
                new NotFoundException("Not found notification content with id: " + id));
    }

}
