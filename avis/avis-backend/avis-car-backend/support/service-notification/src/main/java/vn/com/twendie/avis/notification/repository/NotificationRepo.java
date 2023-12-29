package vn.com.twendie.avis.notification.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import vn.com.twendie.avis.data.model.Notification;

import java.util.Collection;
import java.util.List;

public interface NotificationRepo extends JpaRepository<Notification, Long> {

    Page<Notification> findAllByUserIdAndStatusInAndDeletedFalseOrderByCreatedAtDesc(Long userId, Collection<String> status, Pageable pageable);

    Page<Notification> findAllByUserIdAndDeletedFalseOrderByIdDesc(Long userId, Pageable pageable);

    @Modifying
    @Query(value = "UPDATE notification n SET n.status = ?2 where n.id IN ?1", nativeQuery = true)
    void updateNotificationStatus(List<Integer> id, String status);

}
