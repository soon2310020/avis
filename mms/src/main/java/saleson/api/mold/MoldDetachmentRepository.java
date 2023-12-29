package saleson.api.mold;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import saleson.common.enumeration.NotificationStatus;
import saleson.model.Mold;
import saleson.model.MoldDetachment;

import java.util.List;

public interface MoldDetachmentRepository extends JpaRepository<MoldDetachment, Long>, QuerydslPredicateExecutor<MoldDetachment>, MoldDetachmentRepositoryCustom {
    List<MoldDetachment> findByLatestAndMold(Boolean latest, Mold mold);
    List<MoldDetachment> findByLatest(Boolean latest);
    List<MoldDetachment> findByMoldIdAndNotificationStatusAndLatest(Long moldId, NotificationStatus notificationStatus,Boolean latest);
    List<MoldDetachment> findByMoldIdAndNotificationStatusInAndLatest(Long moldId, List<NotificationStatus> notificationStatus,Boolean latest);
    List<MoldDetachment> findByIdIsIn(List<Long> ids);
    List<MoldDetachment> findByMoldIdIsInAndNotificationStatusAndLatest(List<Long> ids, NotificationStatus notificationStatus,Boolean latest);
    List<MoldDetachment> findByMoldIdIsInAndNotificationStatusInAndLatest(List<Long> ids, List<NotificationStatus> notificationStatus,Boolean latest);
}
