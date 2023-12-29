package saleson.api.shiftConfig;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import saleson.common.enumeration.DayShiftType;
import saleson.model.DayShift;

import java.util.List;

public interface DayShiftRepository extends JpaRepository<DayShift, Long>, QuerydslPredicateExecutor<DayShift> {
    List<DayShift> findByLocationId(Long locationId);

    List<DayShift> findByLocationIdAndDayShiftType(Long locationId, DayShiftType dayShiftType);
}
