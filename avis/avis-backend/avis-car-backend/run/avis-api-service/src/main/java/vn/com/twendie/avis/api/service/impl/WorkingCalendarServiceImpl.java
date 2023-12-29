package vn.com.twendie.avis.api.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import vn.com.twendie.avis.api.core.util.DateUtils;
import vn.com.twendie.avis.api.repository.JourneyDiaryDailyRepo;
import vn.com.twendie.avis.api.repository.WorkingCalendarRepo;
import vn.com.twendie.avis.api.repository.WorkingDayRepo;
import vn.com.twendie.avis.api.service.WorkingCalendarService;
import vn.com.twendie.avis.data.enumtype.WorkingDayEnum;
import vn.com.twendie.avis.data.model.JourneyDiaryDaily;
import vn.com.twendie.avis.data.model.WorkingCalendar;
import vn.com.twendie.avis.data.model.WorkingDay;

import java.io.IOException;
import java.sql.Timestamp;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

import static vn.com.twendie.avis.data.enumtype.ContractTypeEnum.WITH_DRIVER;

@Service
@CacheConfig(cacheNames = "WorkingCalendar")
@Slf4j
public class WorkingCalendarServiceImpl implements WorkingCalendarService {

    private final WorkingCalendarRepo workingCalendarRepo;

    private final WorkingCalendarService workingCalendarService;

    private final DateUtils dateUtils;

    private final WorkingDayRepo workingDayRepo;

    private final JourneyDiaryDailyRepo journeyDiaryDailyRepo;

    public WorkingCalendarServiceImpl(WorkingCalendarRepo workingCalendarRepo,
                                      @Lazy WorkingCalendarService workingCalendarService,
                                      DateUtils dateUtils,
                                      WorkingDayRepo workingDayRepo, JourneyDiaryDailyRepo journeyDiaryDailyRepo) {
        this.workingCalendarRepo = workingCalendarRepo;
        this.workingCalendarService = workingCalendarService;
        this.dateUtils = dateUtils;
        this.workingDayRepo = workingDayRepo;
        this.journeyDiaryDailyRepo = journeyDiaryDailyRepo;
    }

    @Override
    @Cacheable(key = "'all'")
    public List<WorkingCalendar> findAll() {
        return workingCalendarRepo.findAllByDeletedIsFalseOrderByDate();
    }

    @Override
    public boolean isHoliday(Date date, Long workingDayId) {
        java.sql.Date sqlDate = dateUtils.getDate(date);
        return workingCalendarService.findAll()
                .stream()
                .anyMatch(workingCalendar -> dateUtils.getDate(workingCalendar.getDate()).toString().equals(sqlDate.toString()) &&
                        workingCalendar.getWorkingDay().getId().equals(workingDayId) &&
                        workingCalendar.isHoliday());
    }

    @Override
    public boolean isWeekend(Date date, Long workingDayId) {
        java.sql.Date sqlDate = dateUtils.getDate(date);
        return workingCalendarService.findAll()
                .stream()
                .anyMatch(workingCalendar -> dateUtils.getDate(workingCalendar.getDate()).toString().equals(sqlDate.toString()) &&
                        workingCalendar.getWorkingDay().getId().equals(workingDayId) &&
                        workingCalendar.isWeekend());
    }
    public boolean isHoliday(List<WorkingCalendar> allWorkingCalendar,Date date, Long workingDayId) {
        java.sql.Date sqlDate = dateUtils.getDate(date);
        return allWorkingCalendar
                .stream()
                .anyMatch(workingCalendar -> dateUtils.getDate(workingCalendar.getDate()).toString().equals(sqlDate.toString()) &&
                        workingCalendar.getWorkingDay().getId().equals(workingDayId) &&
                        workingCalendar.isHoliday());
    }

    public boolean isWeekend(List<WorkingCalendar> allWorkingCalendar,Date date, Long workingDayId) {
        java.sql.Date sqlDate = dateUtils.getDate(date);
        return allWorkingCalendar
                .stream()
                .anyMatch(workingCalendar -> dateUtils.getDate(workingCalendar.getDate()).toString().equals(sqlDate.toString()) &&
                        workingCalendar.getWorkingDay().getId().equals(workingDayId) &&
                        workingCalendar.isWeekend());
    }

    @Override
    public boolean isWeekendOrHoliday(Date date, Long workingDayId) {
        return isWeekend(date, workingDayId) || isHoliday(date, workingDayId);
    }

    @Override
    public long countWorkingDay(Timestamp from, Timestamp to, Long workingDayId) {
        return dateUtils.getDatesBetween(from, to).stream()
                .filter(date -> !isWeekend(date, workingDayId))
                .count();
    }

    @Override
    public void importWorkingCalendar(int year) {
        if (workingCalendarRepo.existsByYear(year).longValue() == 1) {
            return;
        }
        WorkingDay MON_TO_FRI = workingDayRepo.getOne(WorkingDayEnum.MON_TO_FRI.getId());
        WorkingDay MON_TO_SAT = workingDayRepo.getOne(WorkingDayEnum.MON_TO_SAT.getId());
        WorkingDay FLEXIBLE = workingDayRepo.getOne(WorkingDayEnum.FLEXIBLE.getId());
        LocalDate localDate = LocalDate.of(year, 1, 1);
        List<WorkingCalendar> workingCalendars = new ArrayList<>();
        while (true) {
            workingCalendars.add(createDateWeekend(localDate, MON_TO_FRI));
            workingCalendars.add(createDateWeekend(localDate, MON_TO_SAT));
            workingCalendars.add(createDateWeekend(localDate, FLEXIBLE));
            localDate = localDate.plusDays(1);
            if (localDate.getYear() == year + 1) {
                break;
            }
        }
        workingCalendarRepo.saveAll(workingCalendars);
    }

    @Override
    @Transactional
    public void fixWorkingCalendar(int year) {
        if (workingCalendarRepo.existsByYear(year).longValue() < 1) {
            return;
        }
        List<WorkingCalendar> workingCalendarList = workingCalendarRepo.listWorkingCalendarWrong(year, WorkingDayEnum.MON_TO_SAT.getId());
        workingCalendarList.forEach(s -> fixIsWeekendMonToSat(s));
    }

    private int partitionSize = 100;

    @Override
    @Transactional(rollbackFor = {Exception.class})
    public void fixJourneyDiaryDailyWeekend(Integer type) {
    WorkingDayEnum workingDayEnum=WorkingDayEnum.MON_TO_SAT;
    if(Objects.nonNull(type) && type.intValue()==1) workingDayEnum = WorkingDayEnum.MON_TO_FRI;
    WorkingDayEnum[] workingDayEnums= { workingDayEnum };
        for (int i = 0; ; i++) {
            try {
                Page<JourneyDiaryDaily> originJddPage = journeyDiaryDailyRepo
                        .findByContractContractTypeIdAndContractWorkingDayId(WITH_DRIVER.value(), workingDayEnum.getId(),
                                PageRequest.of(i, partitionSize, Sort.by("id").ascending()));
                if (originJddPage.getContent().size() < 1) break;
                List<JourneyDiaryDaily> journeyDiaryDailies
                        = originJddPage.stream().filter(s -> {
                    Boolean isWeekend = isWeekend(s.getDate(), workingDayEnums[0].getId());
                    if (Objects.isNull(s.getIsWeekend()) || s.getIsWeekend() != isWeekend){
                        s.setIsWeekend(isWeekend);
                        return true;
                    }
                    else return false;
                }).collect(Collectors.toList());
                journeyDiaryDailyRepo.saveAll(journeyDiaryDailies);
                log.info("First element id: " + originJddPage.getContent().get(0).getId());
                log.info("Last element index: " + originJddPage.getContent().get(originJddPage.getContent().size() - 1).getId());
                log.info("Number element updated: " + journeyDiaryDailies.size());
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        }


    }

    public static final String LOCAL_TIME_ZONE = "Asia/Ho_Chi_Minh";

    private void fixIsWeekendMonToSat(WorkingCalendar workingCalendar) {
        DayOfWeek dayOfWeek = new Date(workingCalendar.getDate().getTime()).toInstant().atZone(ZoneId.of(LOCAL_TIME_ZONE)).getDayOfWeek();
        if (dayOfWeek.getValue() == 6 && workingCalendar.isWeekend()) {
            workingCalendar.setWeekend(false);
        }
        if (dayOfWeek.getValue() == 7 && workingCalendar.isWeekend() == false) {
            workingCalendar.setWeekend(true);
        }
    }

    private WorkingCalendar createDateWeekend(LocalDate localDate, WorkingDay workingDay) {
        WorkingCalendar workingCalendar = new WorkingCalendar();
        workingCalendar.setDate(java.sql.Date.valueOf(localDate));
        workingCalendar.setWorkingDay(workingDay);
        if (WorkingDayEnum.MON_TO_SAT.getId().equals(workingDay.getId()) && localDate.getDayOfWeek().getValue() == 7) {
            workingCalendar.setWeekend(true);
        } else if (WorkingDayEnum.MON_TO_FRI.getId().equals(workingDay.getId()) &&
                (localDate.getDayOfWeek().getValue() == 6 || localDate.getDayOfWeek().getValue() == 7)) {
            workingCalendar.setWeekend(true);
        }
        return workingCalendar;
    }

    @Override
    @Transactional(rollbackFor = {Exception.class})
    public synchronized void fixJourneyDiaryDailyMissingWorkingCalendar(Integer year,String fromStr,String toStr) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(year,0,0,0,0);
        Timestamp from= Timestamp.from(calendar.toInstant());
        calendar.add(Calendar.YEAR,1);
        Timestamp to= Timestamp.from(calendar.toInstant());
        if(StringUtils.isNotBlank(fromStr) && StringUtils.isNotBlank(toStr)){
            calendar.set(Integer.valueOf(fromStr.substring(0,4)),Integer.valueOf(fromStr.substring(4,6))-1,Integer.valueOf(fromStr.substring(6,8)),0,0);
            LocalDate localDate = LocalDate.of(Integer.valueOf(fromStr.substring(0,4))
                    ,Integer.valueOf(fromStr.substring(4,6)),Integer.valueOf(fromStr.substring(6,8)));
            from= Timestamp.valueOf(localDate.atStartOfDay());
            localDate = LocalDate.of(Integer.valueOf(toStr.substring(0,4)),Integer.valueOf(toStr.substring(4,6)),Integer.valueOf(toStr.substring(6,8)));
            to= Timestamp.valueOf(localDate.atStartOfDay());
        }
        int total=0;
        List<WorkingCalendar> allWorkingCalendar = workingCalendarService.findAll();
        for (int i = 0; ; i++) {
                Page<JourneyDiaryDaily> originJddPage = journeyDiaryDailyRepo
                        .findByDateBetween(from,to,
                                PageRequest.of(i, partitionSize, Sort.by("date").ascending()));
            try {
                if (originJddPage.getContent().size() < 1) break;
                log.info("From {} to {}", originJddPage.getContent().get(0).getDate(), originJddPage.getContent().get(originJddPage.getContent().size() - 1).getDate());
                List<JourneyDiaryDaily> journeyDiaryDailies
                        = originJddPage.stream().filter(s -> {
                    boolean isChanged = false;
                    if (s.getContract() != null && s.getContract().getWorkingDay() != null) {
                        Long workingDayId = s.getContract().getWorkingDay().getId();
                        Boolean isWeekend = isWeekend(allWorkingCalendar, s.getDate(), workingDayId);
                        Boolean isHoliday = isHoliday(allWorkingCalendar, s.getDate(), workingDayId);
                        if (Objects.isNull(s.getIsWeekend()) || s.getIsWeekend() != isWeekend) {
                            s.setIsWeekend(isWeekend);
                            isChanged = true;
                        }
                        if (Objects.isNull(s.getIsHoliday()) || s.getIsHoliday() != isHoliday) {
                            s.setIsHoliday(isHoliday);
                            isChanged = true;
                        }
                    }
                    return isChanged;
                }).collect(Collectors.toList());
                journeyDiaryDailyRepo.saveAll(journeyDiaryDailies);
                log.info("Number element updated: " + journeyDiaryDailies.size());
                total += journeyDiaryDailies.size();
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        }
        log.info("Total element updated: " + total);
    }


    @Override
    public void importHoliday(MultipartFile holidayFile, Integer year) throws IOException {
        try {
            log.info("Start importHoliday");
            if(holidayFile == null || holidayFile.isEmpty()) return ;

            Workbook workbook = new XSSFWorkbook(holidayFile.getInputStream());
            Sheet sheet = workbook.getSheetAt(0);
            int rowData = 1;
            List<Pair<Date,Integer>> dateList=new ArrayList<>();
            int maxRow = sheet.getLastRowNum();
            for(int r=rowData;r<maxRow;r++){
                String date = ExcelCommonUtils.getOrCreateCell(sheet,r,0).getStringCellValue();
                String type = ExcelCommonUtils.getOrCreateCell(sheet,r,1).toString();
                if(StringUtils.isNotBlank(date) && StringUtils.isNotBlank(type)){
                    try{
                        Date d= DateUtils.getDate(date,DateUtils.SHORT_PATTERN);
                        LocalDate localDate = LocalDate.of(Integer.valueOf(date.substring(6,10)),  Integer.valueOf(date.substring(3,5)), Integer.valueOf(date.substring(0,2)));
                        d= java.sql.Date.valueOf(localDate);
                        Integer typeDate=Double.valueOf(type).intValue();
                        dateList.add(Pair.of(d,typeDate));
                    }catch (Exception e){
                    e.printStackTrace();
                    throw e;
                }
                }
            }

            int[] totalChange = {0};
            if (!dateList.isEmpty()) {
                log.info("dateList: {}", dateList.size());
                dateList.stream().forEach(dateIntegerPair -> {
                    List<WorkingCalendar> calendarList = workingCalendarRepo.findAllByDate(dateIntegerPair.getFirst());
                    log.info("calendarList: {}", calendarList.size());
                    calendarList.stream().forEach(c -> {
                        Integer typeDate = dateIntegerPair.getSecond();
                        if (typeDate.intValue() == 2) {
                            c.setHoliday(true);
                        }
                        if (typeDate.intValue() == 1) {
                            c.setWeekend(true);
                        }
                        if (typeDate.intValue() == 0) {
                            c.setHoliday(false);
                            c.setWeekend(false);
                        }
                        totalChange[0]++;
                    });
                    workingCalendarRepo.saveAll(calendarList);
                    if (dateIntegerPair.getSecond().intValue() == 2 || dateIntegerPair.getSecond().intValue() == 0 ) {
                        Set<Long> workingDayEnumExists= calendarList.stream().map(c->c.getWorkingDay()).filter(w->w!=null && w.getId()!=null)
                                .map(w->w.getId()).collect(Collectors.toSet());
                        List<WorkingDay> workingDayAll = workingDayRepo.findAllByDeletedFalseOrderById();
                        List<WorkingDay> workingDayEnumListNew = workingDayAll.stream().filter(w->!workingDayEnumExists.contains(w.getId())).collect(Collectors.toList());
                        List<WorkingCalendar> workingCalendarsNew = new ArrayList<>();
                        workingDayEnumListNew.stream().forEach(workingDay -> {
                            workingCalendarsNew.add(WorkingCalendar.builder().holiday(dateIntegerPair.getSecond().intValue() == 2)
                                    .date(dateUtils.getDate(dateIntegerPair.getFirst()))
                                            .workingDay(workingDay)
                                    .build());
                        });
                        log.info("workingCalendars add new {}",workingCalendarsNew.size());
                        workingCalendarRepo.saveAll(workingCalendarsNew);

                    }
                });
                new Thread(() -> {
                    if (totalChange[0] > 0) {
                        fixJourneyDiaryDailyMissingWorkingCalendar(year,null,null);
                    }
                }).start();
            }
            log.info("End importHoliday {} changes", totalChange[0]);

        }catch (Exception e){
            e.printStackTrace();
            throw e;
        }

    }
}
