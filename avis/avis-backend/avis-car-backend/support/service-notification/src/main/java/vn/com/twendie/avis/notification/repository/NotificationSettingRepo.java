package vn.com.twendie.avis.notification.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.com.twendie.avis.data.model.NotificationSetting;

import java.util.List;

@Repository
public interface NotificationSettingRepo extends JpaRepository<NotificationSetting, Long> {

    @Query(value = "select * from notification_setting where user_id = :user_id", nativeQuery = true)
    NotificationSetting findByUser(@Param("user_id") Long user_id);

    @Query(value = "select * from notification_setting where user_id in :userIds", nativeQuery = true)
    List<NotificationSetting> findByUserIdIn(@Param("userIds") List<Long> userIds);
}
