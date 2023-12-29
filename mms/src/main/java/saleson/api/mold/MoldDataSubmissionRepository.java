package saleson.api.mold;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import saleson.common.enumeration.NotificationStatus;
import saleson.model.MoldDataSubmission;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface MoldDataSubmissionRepository extends JpaRepository<MoldDataSubmission, Long>, QuerydslPredicateExecutor<MoldDataSubmission>, MoldDataSubmissionRepositoryCustom {
    Optional<MoldDataSubmission> findByMoldIdAndLatest(Long moldId, Boolean latest);

    Optional<MoldDataSubmission> findByMoldIdAndNotificationStatusAndLatest(Long moldId, NotificationStatus notificationStatus, Boolean latest);

    List<MoldDataSubmission> findByMoldIdAndNotificationStatusInAndLatest(Long moldId, List<NotificationStatus> notificationStatusList, Boolean latest);

    List<MoldDataSubmission> findByMoldIdInAndNotificationStatusInAndLatest(List<Long> moldIds, List<NotificationStatus> notificationStatusList, Boolean latest);

    List<MoldDataSubmission> findAllByCreatedAtBetweenAndLatest(Instant startDate, Instant endDate, Boolean latest, Sort sort);

    List<MoldDataSubmission> findByLatestAndNotificationStatusIn(Boolean latest, List<NotificationStatus> notificationStatusList);

    void deleteAllByMoldIdIn(List<Long> moldIdList);
}
