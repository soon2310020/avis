package saleson.api.mold;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import saleson.common.enumeration.NotificationStatus;
import saleson.model.MoldDisconnect;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface MoldDisconnectRepository extends JpaRepository<MoldDisconnect, Long>, QuerydslPredicateExecutor<MoldDisconnect>, MoldDisconnectRepositoryCustom {
	List<MoldDisconnect> findByIdIsIn(List<Long> ids);

	List<MoldDisconnect> findAllByNotificationStatus(NotificationStatus alert);

	List<MoldDisconnect> findAllByCreatedAtBetween(Instant startDate, Instant endDate);

	List<MoldDisconnect> findAllByCreatedAtBetweenAndNotificationStatusAndLatest(Instant startDate, Instant endDate, NotificationStatus status, Boolean latest, Sort sort);

	List<MoldDisconnect> findByMoldIdAndNotificationStatus(Long id, NotificationStatus status);

	List<MoldDisconnect> findByMoldIdIsInAndNotificationStatus(List<Long> ids, NotificationStatus status);

	List<MoldDisconnect> findByMoldIdIsInAndNotificationStatusAndLatest(List<Long> ids, NotificationStatus status, Boolean latest);

	Optional<MoldDisconnect> findByMoldIdAndNotificationStatusAndLatest(Long moldId, NotificationStatus status, Boolean latest);

	List<MoldDisconnect> findByMoldIdAndNotificationStatusIsInAndLatest(Long moldId, List<NotificationStatus> status, Boolean latest);

	List<MoldDisconnect> findByNotificationStatusIsInAndLatest(List<NotificationStatus> status, Boolean latest);

	List<MoldDisconnect> findByLatest(Boolean latest);
}
