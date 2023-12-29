package saleson.api.notification;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import saleson.model.Notification;

import java.util.List;

public interface NotificationRepository  extends JpaRepository<Notification, Long>, QuerydslPredicateExecutor<Notification> {

    List<Notification> findAllBySystemNoteIdAndUserTargetIdIn(Long systemNoteId,List<Long> userTargetIds);
}
