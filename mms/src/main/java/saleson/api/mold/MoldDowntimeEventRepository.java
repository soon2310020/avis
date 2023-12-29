package saleson.api.mold;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import saleson.common.enumeration.DowntimeStatus;
import saleson.model.MoldDowntimeEvent;

import java.time.Instant;
import java.util.List;

public interface MoldDowntimeEventRepository extends JpaRepository<MoldDowntimeEvent, Long>, QuerydslPredicateExecutor<MoldDowntimeEvent>, MoldDownTimeEventRepositoryCustom {
    List<MoldDowntimeEvent> findByMoldIdInAndDowntimeStatusAndLatest(List<Long> moldIds, DowntimeStatus downtimeStatus, Boolean latest);

    List<MoldDowntimeEvent> findAllByCreatedAtBetweenAndDowntimeStatusIsInAndLatest(Instant from, Instant to, List<DowntimeStatus> statuses, Boolean latest, Sort sort);

    List<MoldDowntimeEvent> findByLatest(Boolean latest);
}
