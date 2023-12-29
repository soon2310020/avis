package vn.com.twendie.avis.notification.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.com.twendie.avis.data.model.NotificationContent;

public interface NotificationContentRepo extends JpaRepository<NotificationContent, Long> {
}
