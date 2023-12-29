package vn.com.twendie.avis.api.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import vn.com.twendie.avis.data.model.WorkingCalendar;

import java.math.BigInteger;
import java.util.Date;
import java.util.List;

public interface WorkingCalendarRepo extends JpaRepository<WorkingCalendar, Long> {

    @Query(nativeQuery = true, value = "select EXISTS(select * from working_calendar w where year(w.date) = :year)")
    BigInteger existsByYear(int year);

    @Query(nativeQuery = true, value = "select * from working_calendar w where year(w.date) = :year and w.working_day_id = :id")
    List<WorkingCalendar> listWorkingCalendarWrong(int year, long id);

    List<WorkingCalendar> findAllByDateIn(List<Date> dateList);
    List<WorkingCalendar> findAllByDate(Date date);
    List<WorkingCalendar> findAllByDeletedIsFalseOrderByDate();

}
