package vn.com.twendie.avis.api.service;

import org.springframework.web.multipart.MultipartFile;
import vn.com.twendie.avis.data.model.WorkingCalendar;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

public interface WorkingCalendarService {

    List<WorkingCalendar> findAll();

    boolean isHoliday(Date date, Long workingDayId);

    boolean isWeekend(Date date, Long workingDayId);

    boolean isWeekendOrHoliday(Date date, Long workingDayId);

    long countWorkingDay(Timestamp from, Timestamp to, Long workingDayId);

    void importWorkingCalendar(int year);

    void fixWorkingCalendar(int year);

    void fixJourneyDiaryDailyWeekend(Integer type);

    void fixJourneyDiaryDailyMissingWorkingCalendar(Integer year,String from,String to);
    void importHoliday(MultipartFile holidayFile,Integer year) throws IOException;

}
