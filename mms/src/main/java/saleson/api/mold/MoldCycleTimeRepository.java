package saleson.api.mold;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import saleson.common.enumeration.NotificationStatus;
import saleson.common.enumeration.PeriodType;
import saleson.model.MoldCycleTime;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface MoldCycleTimeRepository extends JpaRepository<MoldCycleTime, Long>, QuerydslPredicateExecutor<MoldCycleTime>,MoldCycleTimeRepositoryCustom {

	Optional<MoldCycleTime> findByMoldIdAndNotificationStatus(Long id, NotificationStatus status);

	List<MoldCycleTime> findByMoldIdAndNotificationStatusAndLatestAndPeriodType(Long id, NotificationStatus status, Boolean latest, PeriodType periodType);

	List<MoldCycleTime> findByMoldIdIsInAndNotificationStatusAndLatest(List<Long> id, NotificationStatus status, Boolean latest);

	List<MoldCycleTime> findByMoldId(Long moldId, Sort sort);

	List<MoldCycleTime> findByIdIsIn(List<Long> ids);

	List<MoldCycleTime> findAllByCreatedAtBetween(Instant startDate, Instant endDate);

//	List<MoldCycleTime> findAllByCreatedAtBetweenAndLatestAndNotificationWeekAtIsNull(Instant startDate, Instant endDate, Boolean latest, Sort sort);
//
//	List<MoldCycleTime> findAllByCreatedAtBetweenAndLatestAndNotificationWeekAtIsNotNull(Instant startDate, Instant endDate, Boolean latest, Sort sort);

	List<MoldCycleTime> findAllByCreatedAtBetweenAndLatestAndPeriodType(Instant startDate, Instant endDate, Boolean latest, PeriodType periodType, Sort sort);

	List<MoldCycleTime> findByLatest(Boolean latest);
}
