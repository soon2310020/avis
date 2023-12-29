package saleson.api.shiftConfig;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import saleson.model.DayShift;
import saleson.model.HourShift;

import java.util.List;

public interface HourShiftRepository extends JpaRepository<HourShift, Long>, QuerydslPredicateExecutor<HourShift> {
    List<HourShift> findByDayShiftIn(List<DayShift> dayShifts);
    List<HourShift> findByDayShiftOrderByIdAsc(DayShift dayShift);
}
