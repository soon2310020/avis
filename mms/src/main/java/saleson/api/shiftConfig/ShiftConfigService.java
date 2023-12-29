package saleson.api.shiftConfig;

import com.emoldino.api.common.resource.base.location.util.LocationUtils;
import com.emoldino.framework.util.DateUtils2;
import com.emoldino.framework.util.ThreadUtils;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.compress.utils.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;
import saleson.api.location.LocationRepository;
import saleson.api.shiftConfig.payload.DayShiftData;
import saleson.api.shiftConfig.payload.ShiftConfigData;
import saleson.api.shiftConfig.payload.ShiftLite;
import saleson.common.constant.CommonMessage;
import saleson.common.enumeration.DayShiftType;
import saleson.common.payload.ApiResponse;
import saleson.common.util.DateUtils;
import saleson.common.util.StringUtils;
import saleson.model.DayShift;
import saleson.model.HourShift;
import saleson.model.Location;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ShiftConfigService {
//    @Autowired
//    private ShiftConfigRepository shiftConfigRepository;
    @Autowired
    private DayShiftRepository dayShiftRepository;
    @Autowired
    private HourShiftRepository hourShiftRepository;
    @Autowired
    private LocationRepository locationRepository;

    public ApiResponse get(Long locationId) {
        try {
            Location location = locationRepository.getOne(locationId);
            ShiftConfigData result = ShiftConfigData.builder()
                    .locationId(locationId)
                    .locationCode(location.getLocationCode())
                    .locationName(location.getName())
                    .build();

            List<DayShift> dayShifts = dayShiftRepository.findByLocationId(locationId);
            processDefaultDayShift(dayShifts, locationId);
            result.setDayShiftData(dayShifts.stream().map(DayShiftData::new).collect(Collectors.toList()));
            return ApiResponse.success(CommonMessage.OK, result);
        } catch (Exception e) {
            e.printStackTrace();
            return ApiResponse.error();
        }
    }

    public void processDefaultDayShift(List<DayShift> dayShifts, Long locationId) {
        List<DayShiftType> existed = dayShifts.stream().map(DayShift::getDayShiftType).collect(Collectors.toList());
        for (DayShiftType type : DayShiftType.values()) {
            if (!existed.contains(type)) {
                dayShifts.add(generateDefaultDayShift(locationId, type));
            }
        }
    }

    public ApiResponse save(ShiftConfigData data) {
        try {
            List<DayShift> oldDayShifts = dayShiftRepository.findByLocationIdAndDayShiftType(data.getLocationId(), data.getDayShiftData().get(0).getDayShiftType());
//            if (DayShiftType.DEFAULT.equals(data.getDayShiftData().get(0).getDayShiftType())) {
//                oldDayShifts = dayShiftRepository.findByLocationId(data.getLocationId());
//            }

            if (CollectionUtils.isNotEmpty(oldDayShifts)) {
                List<HourShift> hourShifts = hourShiftRepository.findByDayShiftIn(oldDayShifts);
                if (CollectionUtils.isNotEmpty(hourShifts))
                    hourShiftRepository.deleteAll(hourShifts);

                dayShiftRepository.deleteAll(oldDayShifts);
            }

            List<DayShift> newDayShifts = saveShiftData(data);
            dayShiftRepository.saveAll(newDayShifts);
            return ApiResponse.success(CommonMessage.OK, newDayShifts);
        } catch (Exception e) {
            e.printStackTrace();
            return ApiResponse.error();
        }
    }

    private List<DayShift> saveShiftData(ShiftConfigData data) {
        List<Future<DayShift>> dayShifts = new ArrayList<>();
        ExecutorService dateExecutorService = Executors.newFixedThreadPool(5);
        ExecutorService shiftExecutorService = Executors.newFixedThreadPool(5);
        data.getDayShiftData().forEach(d -> dayShifts.add(shiftExecutorService.submit( () ->
                {
                    DayShift dayShift = DayShift.builder()
                            .locationId(data.getLocationId())
                            .location(locationRepository.getOne(data.getLocationId()))
                            .numberOfShifts(d.getNumberOfShifts())
                            .start(d.getStart())
                            .end(d.getEnd())
                            .dayShiftType(d.getDayShiftType())
                            .automatic(d.getAutomatic())
                            .hourShifts(Lists.newArrayList())
                            .build();
                    dayShiftRepository.save(dayShift);

                    d.getHourShiftData().forEach(h -> {
                        HourShift hourShift = HourShift.builder()
                                .dayShiftId(dayShift.getId())
                                .dayShift(dayShiftRepository.getOne(dayShift.getId()))
                                .shiftNumber(h.getShiftNumber())
                                .start(h.getStart())
                                .end(h.getEnd())
                                .build();
                        hourShiftRepository.save(hourShift);
                    });



                    if (DayShiftType.DEFAULT.equals(d.getDayShiftType())) {
                        CompletableFuture<Void> allOfFuture  = CompletableFuture.allOf(
                      CompletableFuture.runAsync(() ->cloneDefaultConfigToOtherDays(d.getLocationId(), d, DayShiftType.MONDAY),dateExecutorService),
                      CompletableFuture.runAsync(() ->cloneDefaultConfigToOtherDays(d.getLocationId(), d, DayShiftType.TUESDAY),dateExecutorService),
                      CompletableFuture.runAsync(() ->cloneDefaultConfigToOtherDays(d.getLocationId(), d, DayShiftType.WEDNESDAY),dateExecutorService),
                      CompletableFuture.runAsync(() ->cloneDefaultConfigToOtherDays(d.getLocationId(), d, DayShiftType.THURSDAY),dateExecutorService),
                      CompletableFuture.runAsync(() ->cloneDefaultConfigToOtherDays(d.getLocationId(), d, DayShiftType.FRIDAY),dateExecutorService),
                      CompletableFuture.runAsync(() ->cloneDefaultConfigToOtherDays(d.getLocationId(), d, DayShiftType.SATURDAY),dateExecutorService),
                      CompletableFuture.runAsync(() ->cloneDefaultConfigToOtherDays(d.getLocationId(), d, DayShiftType.SUNDAY),dateExecutorService));
                        try {
                            allOfFuture.get();
                        } catch (InterruptedException | ExecutionException e) {
                            throw new RuntimeException(e);
                        }
                    }
                    return dayShift;


                }
        )));
        List<DayShift> dayShiftList =
         dayShifts.stream().map(
                dsf -> {
                    try {
                       return dsf.get();
                    } catch (InterruptedException | ExecutionException e) {
                        throw new RuntimeException(e);
                    }
                }
        ).collect(Collectors.toList());
        dateExecutorService.shutdown();
        return dayShiftList;
    }

    private void cloneDefaultConfigToOtherDays(Long locationId, DayShiftData d, DayShiftType dayShiftType) {
        List<DayShift> dayShifts =  dayShiftRepository.findByLocationIdAndDayShiftType(locationId,dayShiftType);
        if (CollectionUtils.isNotEmpty(dayShifts)){
            return;
        }
        DayShift dayShift = DayShift.builder()
                .locationId(locationId)
                .location(locationRepository.getOne(locationId))
                .numberOfShifts(d.getNumberOfShifts())
                .start(d.getStart())
                .end(d.getEnd())
                .dayShiftType(dayShiftType)
                .automatic(d.getAutomatic())
                .hourShifts(Lists.newArrayList())
                .build();
        dayShiftRepository.save(dayShift);
        List<Future<HourShift>> hourShiftFutures = new ArrayList<>();
        d.getHourShiftData().forEach(h -> {
            HourShift hourShift = HourShift.builder()
                    .dayShiftId(dayShift.getId())
                    .dayShift(dayShiftRepository.getOne(dayShift.getId()))
                    .shiftNumber(h.getShiftNumber())
                    .start(h.getStart())
                    .end(h.getEnd())
                    .build();
            hourShiftRepository.save(hourShift);
        });
    }

    public DayShift generateDefaultDayShift(Long locationId, DayShiftType dayShiftType) {
        DayShift result = DayShift.builder()
                .locationId(locationId)
                .location(locationRepository.getOne(locationId))
                .numberOfShifts(4L)
                .dayShiftType(dayShiftType)
                .automatic(true)
                .start("0600")
                .end("0600")
                .hourShifts(Lists.newArrayList())
                .build();

        dayShiftRepository.save(result);

        result.getHourShifts().add(new HourShift(result.getId(), result, 1L, "0600", "0700"));
        result.getHourShifts().add(new HourShift(result.getId(), result, 1L, "0700", "0800"));
        result.getHourShifts().add(new HourShift(result.getId(), result, 1L, "0800", "0900"));
        result.getHourShifts().add(new HourShift(result.getId(), result, 1L, "0900", "1000"));
        result.getHourShifts().add(new HourShift(result.getId(), result, 1L, "1000", "1100"));
        result.getHourShifts().add(new HourShift(result.getId(), result, 1L, "1100", "1200"));

        result.getHourShifts().add(new HourShift(result.getId(), result, 2L, "1200", "1300"));
        result.getHourShifts().add(new HourShift(result.getId(), result, 2L, "1300", "1400"));
        result.getHourShifts().add(new HourShift(result.getId(), result, 2L, "1400", "1500"));
        result.getHourShifts().add(new HourShift(result.getId(), result, 2L, "1500", "1600"));
        result.getHourShifts().add(new HourShift(result.getId(), result, 2L, "1600", "1700"));
        result.getHourShifts().add(new HourShift(result.getId(), result, 2L, "1700", "1800"));

        result.getHourShifts().add(new HourShift(result.getId(), result, 3L, "1800", "1900"));
        result.getHourShifts().add(new HourShift(result.getId(), result, 3L, "1900", "2000"));
        result.getHourShifts().add(new HourShift(result.getId(), result, 3L, "2000", "2100"));
        result.getHourShifts().add(new HourShift(result.getId(), result, 3L, "2100", "2200"));
        result.getHourShifts().add(new HourShift(result.getId(), result, 3L, "2200", "2300"));
        result.getHourShifts().add(new HourShift(result.getId(), result, 3L, "2300", "0000"));

        result.getHourShifts().add(new HourShift(result.getId(), result, 4L, "0000", "0100"));
        result.getHourShifts().add(new HourShift(result.getId(), result, 4L, "0100", "0200"));
        result.getHourShifts().add(new HourShift(result.getId(), result, 4L, "0200", "0300"));
        result.getHourShifts().add(new HourShift(result.getId(), result, 4L, "0300", "0400"));
        result.getHourShifts().add(new HourShift(result.getId(), result, 4L, "0400", "0500"));
        result.getHourShifts().add(new HourShift(result.getId(), result, 4L, "0500", "0600"));

        hourShiftRepository.saveAll(result.getHourShifts());

        return result;
    }

    public ApiResponse getShiftListByLocationAndDay(Long locationId, String day) {
        try {
            List<ShiftLite> result = new ArrayList<>();
            DayShiftType dayShiftType = getDayShiftType(day, locationId);
            List<DayShift> dayShifts = dayShiftRepository.findByLocationIdAndDayShiftType(locationId, dayShiftType);
            long numberOfShift = CollectionUtils.isNotEmpty(dayShifts) ? dayShifts.get(0).getNumberOfShifts() : 4;
            for (int i = 1; i <= numberOfShift; i++) {
                result.add(ShiftLite.builder()
                                .name("Shift " + i)
                                .shiftNumber(i)
                        .build());
            }
            return ApiResponse.success(CommonMessage.OK, result);
        } catch (Exception e) {
            e.printStackTrace();
            return ApiResponse.error();
        }
    }

    public DayShiftType getDayShiftType(String dayStr, Long locationId) {
        Instant day = DateUtils2.toInstant(dayStr, DateUtils2.DatePattern.yyyyMMdd, LocationUtils.getZoneIdByLocationId(locationId));
        int dayInt = DateUtils.getDayOfWeek(day);
        switch (dayInt) {
            case 1: return DayShiftType.SUNDAY;
            case 2: return DayShiftType.MONDAY;
            case 3: return DayShiftType.TUESDAY;
            case 4: return DayShiftType.WEDNESDAY;
            case 5: return DayShiftType.THURSDAY;
            case 6: return DayShiftType.FRIDAY;
            case 7: return DayShiftType.SATURDAY;
            default: return DayShiftType.DEFAULT;
        }
    }


    public Pair<String, String> getStartEndByHourShift(Long locationId, String startDate, String endDate) {
        DayShift startShift = getDayShift(locationId, startDate);
        DayShift endShift = getDayShift(locationId, endDate);

        List<HourShift> startHourShifts = hourShiftRepository.findByDayShiftOrderByIdAsc(startShift)
                .stream()
                .filter(h -> !StringUtils.isEmpty(h.getStart()) && !StringUtils.isEmpty(h.getEnd()))
                .collect(Collectors.toList());
        List<HourShift> endHourShifts = hourShiftRepository.findByDayShiftOrderByIdAsc(endShift)
                .stream()
                .filter(h -> !StringUtils.isEmpty(h.getStart()) && !StringUtils.isEmpty(h.getEnd()))
                .collect(Collectors.toList());
        String start = startDate + startHourShifts.get(0).getStart().substring(0,2);
        String end;
        if (isShiftConfigContains2Days(endHourShifts)) {
            endDate += endHourShifts.get(endHourShifts.size() - 1).getEnd().substring(0,2);
            Instant currentDay = DateUtils2.toInstant(endDate, DateUtils2.DatePattern.yyyyMMddHH, LocationUtils.getZoneIdByLocationId(locationId));
//            String nextDay = DateUtils.getNextDay(currentDay, DateUtils.YYYY_MM_DD_DATE_FORMAT, 1);
            Instant theNextDay = currentDay.plus(1, ChronoUnit.DAYS);
            String nextDay = DateUtils2.format(theNextDay, DateUtils2.DatePattern.yyyyMMddHH, LocationUtils.getZoneIdByLocationId(locationId));
            
//            end = nextDay + endHourShifts.get(endHourShifts.size() - 1).getEnd().substring(0,2);
            end = nextDay;
            System.out.println("End end end: " + end);
        } else {
            end = endDate + endHourShifts.get(endHourShifts.size() - 1).getEnd().substring(0,2);
            System.out.println("End: " + end);
        }

        return Pair.of(start, end);
    }

    public boolean isShiftConfigContains2Days(List<HourShift> hourShifts) {
        if (hourShifts.get(0).getStart().isEmpty()||hourShifts.get(hourShifts.size() - 1).getEnd().isEmpty()){
            return false;
        }
        int start = Integer.parseInt(hourShifts.get(0).getStart().substring(0,2));
        int end = Integer.parseInt(hourShifts.get(hourShifts.size() - 1).getEnd().substring(0,2));
        return end <= start;
    }

    public DayShift getDayShift(Long locationId, String date) {
        DayShiftType type = getDayShiftType(date, locationId);
        List<DayShift> dayShifts = dayShiftRepository.findByLocationIdAndDayShiftType(locationId, type);
        DayShift dayShift;
        if (CollectionUtils.isNotEmpty(dayShifts)) {
            dayShift = dayShifts.get(0);
        } else {
            dayShift = generateDefaultDayShift(locationId, type);
        }
        return dayShift;
    }

    public Pair<Instant, Instant> getStartEndOfDayShift(DayShift dayShift, List<Long> shiftNumbers, String day) {
        List<HourShift> hourShifts = dayShift.getHourShifts();
        if (CollectionUtils.isNotEmpty(shiftNumbers)) {
            hourShifts = hourShifts.stream().filter(hourShift -> shiftNumbers.contains(hourShift.getShiftNumber())).collect(Collectors.toList());
        }
        String start = day + hourShifts.get(0).getStart().substring(0,2);
        String end;
        if (isShiftConfigContains2Days(hourShifts)) {
            Instant currentDay = DateUtils.getInstant(day, DateUtils.YYYY_MM_DD_DATE_FORMAT);
            String nextDay = DateUtils.getNextDay(currentDay, DateUtils.YYYY_MM_DD_DATE_FORMAT, 1);
            end = nextDay + hourShifts.get(hourShifts.size() - 1).getEnd().substring(0,2);
        } else {
            end = day + hourShifts.get(hourShifts.size() - 1).getEnd().substring(0,2);
        }
        return Pair.of(DateUtils2.toInstant(start, "yyyyMMddHH", DateUtils2.Zone.SYS), DateUtils.getEndOffHour(DateUtils2.toInstant(end, "yyyyMMddHH", DateUtils2.Zone.SYS)));
    }
}
