package saleson.api.broadcastNotification;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import saleson.model.BroadcastNotification;

import java.util.List;

public interface BroadcastNotificationRepository  extends JpaRepository<BroadcastNotification, Long>, QuerydslPredicateExecutor<BroadcastNotification> {
    Long countAllByUserIdAndIsReadIsFalse(Long userId);
    List<BroadcastNotification> findAllByUserIdAndIsReadIsFalse(Long userId);
}
