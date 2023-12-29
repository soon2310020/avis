package saleson.api.mold;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import saleson.common.enumeration.NotificationStatus;
import saleson.common.enumeration.PeriodType;
import saleson.model.MoldEfficiency;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface MoldEfficiencyRepository extends JpaRepository<MoldEfficiency, Long>, QuerydslPredicateExecutor<MoldEfficiency>, MoldEfficiencyRepositoryCustom {

	List<MoldEfficiency> findByMoldIdAndNotificationStatus(Long id, NotificationStatus status);

	List<MoldEfficiency> findByMoldIdIsInAndNotificationStatus(List<Long> ids, NotificationStatus status);

	Optional<MoldEfficiency> findByMoldIdAndNotificationStatusAndLatest(Long moldId, NotificationStatus status, Boolean latest);

	List<MoldEfficiency> findByMoldIdIsInAndNotificationStatusAndLatest(List<Long> moldIds, NotificationStatus status, Boolean latest);

	List<MoldEfficiency> findByMoldIdAndNotificationStatusAndLatestAndPeriodType(Long moldId, NotificationStatus status, Boolean latest, PeriodType type);

	List<MoldEfficiency> findByMoldId(Long moldId);

	List<MoldEfficiency> findByIdIsIn(List<Long> ids);

	List<MoldEfficiency> findAllByCreatedAtBetween(Instant startDate, Instant endDate);

	List<MoldEfficiency> findAllByCreatedAtBetweenAndLatestAndPeriodType(Instant startDate, Instant endDate, Boolean latest, PeriodType periodType, Sort sort);

//	List<MoldEfficiency> findAllByCreatedAtBetweenAndLatestAndNotificationWeekAtIsNull(Instant startDate, Instant endDate, Boolean latest, Sort sort);
//
//	List<MoldEfficiency> findAllByCreatedAtBetweenAndLatestAndNotificationWeekAtIsNotNull(Instant startDate, Instant endDate, Boolean latest, Sort sort);

	List<MoldEfficiency> findByLatest(Boolean latest);
}
