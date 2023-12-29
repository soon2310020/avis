package saleson.api.machine;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import javax.transaction.Transactional;

import com.emoldino.api.common.resource.base.location.util.LocationUtils;
import com.emoldino.api.common.resource.base.option.service.OptionService;
import com.emoldino.framework.util.BeanUtils;
import com.emoldino.framework.util.DateUtils2;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;

import com.querydsl.core.types.Predicate;

import lombok.extern.slf4j.Slf4j;
import saleson.api.company.CompanyRepository;
import saleson.api.location.LocationRepository;
import saleson.api.machine.payload.*;
import saleson.api.machineDowntimeAlert.MachineDowntimeAlertRepository;
import saleson.api.mold.MoldRepository;
import saleson.api.rejectedPart.ProducedPartRepository;
import saleson.api.rejectedPart.payload.RejectedPartPayload;
import saleson.api.resource.ResourceHandler;
import saleson.api.shiftConfig.DayShiftRepository;
import saleson.api.shiftConfig.HourShiftRepository;
import saleson.api.shiftConfig.ShiftConfigService;
import saleson.api.statistics.StatisticsPartRepository;
import saleson.api.statistics.StatisticsRepository;
import saleson.common.constant.CommonMessage;
import saleson.common.enumeration.*;
import saleson.common.payload.ApiResponse;
import saleson.common.util.DataUtils;
import saleson.common.util.DateUtils;
import saleson.common.util.StringUtils;
import saleson.dto.common.TwoObject;
import saleson.model.*;
import saleson.model.data.MachineData;
import saleson.model.data.MachineStatisticsData;
import saleson.model.rejectedPartRate.ProducedPart;
import saleson.service.transfer.CdataRepository;
import saleson.service.util.DateTimeUtils;
import saleson.service.util.NumberUtils;

@Slf4j
@Service
public class MachineStatisticsService {

    @Autowired
    private MachineStatisticsRepository machineStatisticsRepository;
    @Autowired
    private MachineRepository machineRepository;
    @Autowired
    private MachineMoldMatchingHistoryRepository machineMoldMatchingHistoryRepository;
    @Autowired
    private StatisticsRepository statisticsRepository;
    @Autowired
    private StatisticsPartRepository statisticsPartRepository;
    @Autowired
    private CdataRepository cdataRepository;
    @Autowired
    private CompanyRepository companyRepository;
    @Autowired
    private RiskLevelRepository riskLevelRepository;
    @Autowired
    private DowntimeItemRepository downtimeItemRepository;
    @Autowired
    ProducedPartRepository producedPartRepository;
    @Autowired
    MoldRepository moldRepository;
    @Autowired
    MachineDowntimeAlertRepository machineDowntimeAlertRepository;
    @Autowired
    DayShiftRepository dayShiftRepository;
    @Autowired
    HourShiftRepository hourShiftRepository;
    @Autowired
    MachineOeeRepository machineOeeRepository;
    @Autowired
    LocationRepository locationRepository;
    @Autowired
    ShiftConfigService shiftConfigService;
    @Autowired
    ResourceHandler resourceHandler;


//    public Page<MachineAvailabilityConfigDTO> getAllMachineAvailabilityConfig_old(Predicate predicate, Pageable pageable) {
//        Page<Machine> machinePage = machineRepository.findAllMachineForStatistics(predicate, pageable);
//        List<Machine> machines = machinePage.getContent();
//
//        List<MachineAvailabilityConfigDTO> data = new ArrayList<>();
//        List<MachineStatistics> statisticsList = machineStatisticsRepository.findByMachineIn(machines);
//        Map<Long, List<MachineStatistics>> map =
//                statisticsList.stream()
//                        .flatMap(statistics -> {
//                            Map<Long, MachineStatistics> um = new HashMap<>();
//                            um.put(statistics.getMachine().getId(), statistics);
//                            return um.entrySet().stream();
//                        })
//                        .collect(Collectors.groupingBy(Map.Entry::getKey,
//                                Collectors.mapping(Map.Entry::getValue,
//                                        Collectors.toList())));
//        map.forEach((k, v) -> data.add(new MachineAvailabilityConfigDTO(k, v.stream().map(MachineStatisticsDetails::new).collect(Collectors.toList()))));
//        return new PageImpl<>(data, pageable, machinePage.getTotalElements());
//    }

    public Page<MachineStatisticsData> getAllMachineAvailabilityConfig(Predicate predicate, Pageable pageable, String day) {
        Sort.Direction[] directions = {Sort.Direction.DESC};
        pageable.getSort().forEach(order -> {
            directions[0] = order.getDirection();
        });
        String formattedDay = day.substring(0, 8);
        Page<MachineData> machinePage = machineRepository.findAllMachineForStatistics(predicate, pageable, formattedDay);
        List<Machine> machines = machinePage.getContent().stream().map(MachineData::getMachine).collect(Collectors.toList());

        List<MachineStatisticsData> realData = new ArrayList<>();
        for (Machine machine : machines) {
            Optional<MachineStatistics> optional = machineStatisticsRepository.findByMachineAndDay(machine, formattedDay);
            if (optional.isPresent()) {
                MachineStatisticsData data = new MachineStatisticsData(optional.get());
                List<DowntimeItem> downtimeItems = downtimeItemRepository.findByMachineStatistics(optional.get());
                List<DowntimeItemData> itemData = downtimeItems.stream().map(DowntimeItemData::new).collect(Collectors.toList());
                customizeDowntimeData(itemData);
                data.setDowntimeItems(itemData);
                realData.add(data);
            } else realData.add(new MachineStatisticsData(machine));
        }

        return new PageImpl<>(realData, pageable, machinePage.getTotalElements());
    }

    private void customizeDowntimeData(List<DowntimeItemData> items) {
        items.forEach(i -> {
            if (i.getHourTo() == 23 && i.getMinuteTo() == 60) {
                i.setMinuteTo(59);
            }
        });
    }

    public MachineStatisticsData getMachineStatisticsDataByDay(Long machineId, String day) {
        String formattedDay = day.substring(0, 8);
        Machine machine = machineRepository.getOne(machineId);
        Optional<MachineStatistics> optional = machineStatisticsRepository.findByMachineAndDay(machine, formattedDay);
        MachineStatisticsData data;
        if (optional.isPresent()) {
            data = new MachineStatisticsData(optional.get());
            List<DowntimeItem> downtimeItems = downtimeItemRepository.findByMachineStatistics(optional.get());
            List<DowntimeItemData> itemData = downtimeItems.stream().map(DowntimeItemData::new).collect(Collectors.toList());
            customizeDowntimeData(itemData);
            data.setDowntimeItems(itemData);
        } else data = new MachineStatisticsData(machine);

        return data;
    }

    public Boolean checkExistedConfig(String start, String end, Long machineId, MachineAvailabilityType type) {
        Machine machine = machineRepository.getOne(machineId);
        Instant startDate = DateUtils.getInstant(start, DateUtils.DEFAULT_DATE_FORMAT);
        Instant endDate = DateUtils.getInstant(end, DateUtils.DEFAULT_DATE_FORMAT);

        Optional<List<MachineStatistics>> statisticsOptional;
        if (type.equals(MachineAvailabilityType.DAILY_WORKING_HOUR)) {
            statisticsOptional = machineStatisticsRepository.findByMachineAndDateBetweenAndDailyWorkingHourNotNull(machine, startDate, endDate);
        } else if (type.equals(MachineAvailabilityType.PLANNED_DOWNTIME)) {
            statisticsOptional = machineStatisticsRepository.findByMachineAndDateBetweenAndPlannedDowntimeNotNull(machine, startDate, endDate);
        } else {
            statisticsOptional = machineStatisticsRepository.findByMachineAndDateBetweenAndUnplannedDowntimeNotNull(machine, startDate, endDate);
        }
        if (statisticsOptional.isPresent()) {
            if (CollectionUtils.isNotEmpty(statisticsOptional.get())) {
                return true;
            } else return false;
        } else return false;
    }

    @Transactional
    public List<MachineStatistics> updateConfig(MachineStatisticsPayload payload, MachineAvailabilityType type) {
        String dayPattern = "yyyyMMdd";
        if (type.equals(MachineAvailabilityType.DAILY_WORKING_HOUR) && payload.getEndDateStr().endsWith("000000"))
            payload.setEndDateStr(payload.getEndDateStr().substring(0,8) + "000100");
        List<String> days = DateUtils.getListStringDateBetween(payload.getStartDateStr(), payload.getEndDateStr(), DateUtils.DEFAULT_DATE_FORMAT, dayPattern);

        List<MachineStatistics> listConfig = new ArrayList<>();

        Machine machine = machineRepository.getOne(payload.getMachineId());

        int firstDayHour = Integer.parseInt(payload.getStartDateStr().substring(8,10));
        int firstDayMinute = Integer.parseInt(payload.getStartDateStr().substring(10,12));
        int lastDayHour;
        int lastDayMinute;
        if (payload.getEndDateStr().endsWith("000000")) {
            lastDayHour = 23;
            lastDayMinute = 60;
        } else {
            lastDayHour = Integer.parseInt(payload.getEndDateStr().substring(8,10));
            lastDayMinute = Integer.parseInt(payload.getEndDateStr().substring(10,12));
        }

        ApiResponse checkConflict = checkConflict(type, payload);
        Warning warning = (Warning) checkConflict.getData();
        List<String> conflictDays = CollectionUtils.isNotEmpty(warning.getDays()) ? warning.getDays() : new ArrayList<>();
        if (days.size() == 1) {
            MachineStatistics machineStatistics = checkOverlapped(machine, days.get(0));
            boolean overwrite = conflictDays.contains(days.get(0));
            TwoObject<Integer, Integer> hourAndMinute = calculateDowntimeForTheSameDay(firstDayHour, firstDayMinute, lastDayHour, lastDayMinute);
            TwoObject<Integer, Integer> from = new TwoObject<>(firstDayHour, firstDayMinute);
            TwoObject<Integer, Integer> to = new TwoObject<>(lastDayHour, lastDayMinute);
            if (firstDayHour == firstDayMinute &&
                    firstDayHour == 0 &&
                    lastDayHour == lastDayMinute &&
                    lastDayHour == 0
            ) {
                hourAndMinute = new TwoObject<>(23, 60);
                from = new TwoObject<>(0, 0);
                to = new TwoObject<>(23, 60);
            }
            bindData(machineStatistics, payload, machine, days.get(0), type, hourAndMinute, from, to, overwrite);
        } else {
            days.forEach(day -> {
                boolean overwrite = conflictDays.contains(day);
                MachineStatistics machineStatistics = checkOverlapped(machine, day);
                TwoObject<Integer, Integer> hourAndMinute;
                TwoObject<Integer, Integer> from;
                TwoObject<Integer, Integer> to;


                if (days.indexOf(day) == 0) {
                    hourAndMinute = calculateDowntimeForFirstDay(firstDayHour, firstDayMinute);
                    from = new TwoObject<>(firstDayHour, firstDayMinute);
                    to = new TwoObject<>(23, 60);
                } else if (days.indexOf(day) == (days.size() - 1)) {
                    hourAndMinute = new TwoObject<>(lastDayHour, lastDayMinute);
                    from = new TwoObject<>(0, 0);
                    to = new TwoObject<>(lastDayHour, lastDayMinute);
                } else {
                    hourAndMinute = new TwoObject<>(23, 60);
                    from = new TwoObject<>(0, 0);
                    to = new TwoObject<>(23, 60);
                }
                bindData(machineStatistics, payload, machine, day, type, hourAndMinute, from, to, overwrite);

                listConfig.add(machineStatistics);
            });
        }
//        }

//        machineStatisticsRepository.saveAll(listConfig);

        //update oee
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            String from = payload.getStartDateStr();
            String to = payload.getEndDateStr();

            updateOEEByDayRange(from, to);
        });
        executor.shutdown();

        return listConfig;
    }

    private MachineStatistics checkOverlapped(Machine machine, String day) {
        Optional<MachineStatistics> optional = machineStatisticsRepository.findByMachineAndDay(machine, day);
        return optional.orElseGet(MachineStatistics::new);
    }

//	@Scheduled(cron = "0 0 6 * * *")
//	public void updateDailyOEE() {
//		JobUtils.runIfNotRunning("MachineStatisticsService::updateDailyOEE", new JobOptions().setClustered(true).setAccessSummaryLogEnabled(true), () -> {
//			String yesterday = DateUtils.getYesterday("yyyyMMdd");
//			updateOEEByDay(yesterday);
//		});
//	}

    public void updateOEEByDayRange(String from, String to) {
        String dayPattern = "yyyyMMdd";
        List<String> daysBetween = DateUtils.getListStringDateBetween(from, to, DateUtils.DEFAULT_DATE_FORMAT, dayPattern);
        daysBetween.forEach(this::updateOEEByDay);
    }

    public void updateOEEByDay(String day) {

        List<Machine> machines = machineRepository.findAll();

        List<MachineStatistics> listToUpdate = new ArrayList<>();
        machines.forEach(machine -> {
            Double fa = 0D;
            Double fp = 0D;
            Double fq = 0D;

            Optional<MachineStatistics> optional = machineStatisticsRepository.findByMachineAndDay(machine, day);
            MachineStatistics statistics;
            if (optional.isPresent()) {
                statistics = optional.get();
            } else {
                statistics = new MachineStatistics();
                statistics.setMachine(machine);
                statistics.setDay(day);
                statistics.setDate(DateUtils.getInstant(day + "000000", DateUtils.DEFAULT_DATE_FORMAT));
            }

            //Calculate fa
            Double hw = statistics.roundedDailyWorkHour();
            Double hp = statistics.roundedPlannedDowntime();
            Double hu = statistics.roundedUnplannedDowntime();
            fa = ((hw - hp - hu) / ((hw - hp) == 0 ? 1 : (hw - hp))) * 100;
            statistics.setFa(fa);

            //Calculate fp
            Optional<List<MachineMoldMatchingHistory>> optionalList = machineMoldMatchingHistoryRepository.findByMachineAndMatchDayOrUnmatchDay(machine, day);
            if (!optionalList.isPresent()) {
                Optional<MachineMoldMatchingHistory> history = machineMoldMatchingHistoryRepository.findByMachineAndDay(machine, day);
                if (history.isPresent()) {
                    fp = calculateFp(history.get().getMold().getId(), day, hw, hp, hu);
                } else {
                    if (machine.getMold() == null) {
                        fp = 0D;
                    } else {
                        fp = calculateFp(machine.getMold().getId(), day, hw, hp, hu);
                    }
                }

            } else {
                //todo calculate fp when match/unmatch within a day
                fp = calculateFpWhenMatchOrUnmatch(machine, day, hw, hp, hu);
            }
            statistics.setFp(fp);

            //Calculate fq
            fq = 90D; //fixed
            statistics.setFq(fq);
//            List<ProducedPart> producedParts = producedPartRepository.findByMoldIdAndDay(machine.getMold().getId(), yesterday);
//            Integer totalRejected = producedParts.stream().mapToInt(ProducedPart::getTotalRejectedAmount).sum();
//            Integer totalProduced = producedParts.stream().mapToInt(ProducedPart::getTotalProducedAmount).sum();
//            fq = (double)(totalRejected / totalProduced) * 100;

            double oee = (fa / 100 * fp / 100 * fq / 100) * 100;
            statistics.setOee(oee);
        });
    }

    public ApiResponse getOeeDetailMock(OeePayload payload, Pageable pageable) {
        try {
            return ApiResponse.success(CommonMessage.OK, mockDataOeeDetail(pageable));
        } catch (Exception e) {
            e.printStackTrace();
            return ApiResponse.error();
        }
    }

    public ApiResponse getOeeDetail(OeePayload payload, Pageable pageable) {
        try {
            DayShiftType dayShiftType = shiftConfigService.getDayShiftType(payload.getStart(), payload.getLocationIds().get(0));
            DayShift dayShift;
            List<DayShift> dayShifts = dayShiftRepository.findByLocationIdAndDayShiftType(payload.getLocationIds().get(0), dayShiftType);
            if (CollectionUtils.isNotEmpty(dayShifts)) {
                dayShift = dayShifts.get(0);
            } else {
                dayShift = shiftConfigService.generateDefaultDayShift(payload.getLocationIds().get(0), dayShiftType);
            }

            MachinePayload machinePayload = new MachinePayload();
            machinePayload.setStatus("enabled");
            machinePayload.setLocationId(payload.getLocationIds().get(0));
            machinePayload.setQueryMobile(payload.getQueryMobile());
            if (CollectionUtils.isNotEmpty(payload.getMachineIds())) {
                machinePayload.setIds(payload.getMachineIds());
            }

            Pageable customPageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), Sort.by(Sort.Direction.ASC, "machineCode"));
            Page<Machine> machines = machineRepository.findAllOrderBySpecial(machinePayload.getQueryMobile(), machinePayload.getPredicate(), customPageable);
            return ApiResponse.success(CommonMessage.OK, getDataOeeDetail(machines, pageable, dayShift, payload.getStart(), payload.getShiftNumbers(), payload.getColorCodeConfig()));
        } catch (Exception e) {
            e.printStackTrace();
            return ApiResponse.error();
        }
    }

    private Pair<Instant, Instant> getStartEndDayShift(String day, DayShift dayShift, Machine machine) {
        int firstHourShift = Integer.parseInt(dayShift.getStart());
        int checkedHourShift = Integer.parseInt(dayShift.getEnd());
        String timeZone = LocationUtils.getZoneIdByLocationId(machine.getLocationId());
        String endDay = day;
        if (firstHourShift >= checkedHourShift) {
            Instant currentDay = DateUtils2.toInstant(day, DateUtils.YYYY_MM_DD_DATE_FORMAT, timeZone);
            endDay = DateUtils.getNextDay(currentDay, DateUtils.YYYY_MM_DD_DATE_FORMAT, 1);
        }
        String start = day+dayShift.getStart().substring(0,2);
        String end = endDay+dayShift.getEnd().substring(0,2);
        return Pair.of(DateUtils2.toInstant(start, "yyyyMMddHH", timeZone), DateUtils2.toInstant(end, "yyyyMMddHH", timeZone));
    }

    private Page<OeeDetail> getDataOeeDetail(Page<Machine> machines, Pageable pageable, DayShift dayShift, String day, List<Long> shiftNumbers, String colorCodeConfig) {
        List<OeeDetail> result = new ArrayList<>();

        List<HourShift> hourShiftList = dayShift.getHourShifts().stream()
                .filter(h -> (!CollectionUtils.isNotEmpty(shiftNumbers) || shiftNumbers.contains(h.getShiftNumber()))&&!h.getEnd().isEmpty()&&!h.getStart().isEmpty()).collect(Collectors.toList());

        CustomBoolean switchDay = new CustomBoolean(false);
        Map<HourShift, List<String>> mapHourShiftToHours = mapHourShiftToListHours(dayShift.getHourShifts(), day, switchDay)
                .entrySet()
                .stream()
                .filter(a -> hourShiftList.contains(a.getKey()))
                .collect(Collectors.toMap(Map.Entry::getKey, a-> a.getValue()==null ? new ArrayList<>() :a.getValue()));
        String fromDay = day;
        String toDay;
        if (shiftConfigService.isShiftConfigContains2Days(dayShift.getHourShifts())) {
            Instant currentDay = DateUtils.getInstant(day, DateUtils.YYYY_MM_DD_DATE_FORMAT);
            toDay = DateUtils.getNextDay(currentDay, DateUtils.YYYY_MM_DD_DATE_FORMAT, 1);
        } else {
            toDay = day;
        }



        String fromTimeStr = fromDay.concat(hourShiftList.get(0).getStart());
        String toTimeStr = toDay.concat(hourShiftList.stream().filter(h -> !h.getEnd().isEmpty()).reduce((first,second)-> second).get().getEnd());

        ExecutorService machinePool = Executors.newFixedThreadPool(2);
        List<CompletableFuture<OeeDetail>> machineFutures = new ArrayList<>();
        Map<String, Object> option = BeanUtils.get(OptionService.class).getFieldValues(ConfigCategory.OPTIMAL_CYCLE_TIME);
        String oct;
        if (option.containsKey("strategy")) {
            oct = String.valueOf(option.get("strategy"));
        } else {
            oct = "WACT";
        }


        try {
            machines.getContent().forEach(machine -> {

                Pair<Instant, Instant> startEndDayShift = getStartEndDayShift(day, dayShift, machine);
                CompletableFuture<OeeDetail> oeeDetailCompletableFuture = CompletableFuture.supplyAsync(() -> {
                    List<OeeByShift> oeeByShifts = new ArrayList<>();



                    List<MachineDowntimeAlert> machineDowntimeAlertDataList = machineDowntimeAlertRepository
                            .findByMachineInAndDowntimeOverlapped(Collections.singletonList(machine), startEndDayShift.getFirst(), startEndDayShift.getSecond());


                    List<MachineMoldMatchingHistory> machineMoldMatchingHistoryList = machineMoldMatchingHistoryRepository.findAllMatchingByMatchingDayBetweenAndMachineId(fromTimeStr, toTimeStr, machine.getId());
                    String latestMachineOeeHour = machineOeeRepository.findFirstHourByMachineIdAndPeriodTypeOrderByHourDesc(machine.getId());
                    ExecutorService hourShiftPool = Executors.newFixedThreadPool(5);
                    List<CompletableFuture<OeeByShift>> shiftFutures = new ArrayList<>();
                    try {
                        hourShiftList.stream().filter(h -> CollectionUtils.isNotEmpty(mapHourShiftToHours.get(h))).forEach(hourShift -> {
                            CompletableFuture<OeeByShift> oeeByShiftCompletableFuture = CompletableFuture.supplyAsync(() -> getSingleShiftOee(hourShift, mapHourShiftToHours.get(hourShift), machine, day, machineMoldMatchingHistoryList, hourShiftList, colorCodeConfig, latestMachineOeeHour, oct, machineDowntimeAlertDataList), hourShiftPool);
                            shiftFutures.add(oeeByShiftCompletableFuture);
                        });
                        shiftFutures.forEach(sf -> {
                            try {
                                oeeByShifts.add(sf.get());
                            } catch (InterruptedException | ExecutionException e) {
                                throw new RuntimeException(e);
                            }
                        });

                    } finally {
                        hourShiftPool.shutdown();
                    }

                    List<OeeByShift> sortedOeeByShifts = oeeByShifts.stream().sorted(Comparator.comparing(OeeByShift::getStart)).collect(Collectors.toList());

                    OeeByShift startBlock = sortedOeeByShifts.stream().filter(o -> o.getStart() != null && o.getStart().equals(hourShiftList.get(0).getStart())).findAny().get();
                    int startIndex = sortedOeeByShifts.indexOf(startBlock);

                    List<OeeByShift> finalOeeByShifts = sortedOeeByShifts.subList(startIndex, sortedOeeByShifts.size());
                    finalOeeByShifts.addAll(sortedOeeByShifts.subList(0, startIndex));

                    //expected hourly production
                    List<ExpectedHourlyProduction> expectedHourlyProductionList = new ArrayList<>();

                    machineMoldMatchingHistoryList.forEach(machineMoldMatchingHistory -> {
                        ExpectedHourlyProduction expectedHourlyProduction = new ExpectedHourlyProduction();
                        Mold mold = machineMoldMatchingHistory.getMold();

                        expectedHourlyProduction.setMoldCode(mold.getEquipmentCode());

                        String startHour;
                        String endHour;

                        if (machineMoldMatchingHistory.getMatchDay() != null
                                && ((machineMoldMatchingHistory.getMatchDay().equals(fromDay) && Integer.parseInt(machineMoldMatchingHistory.getMatchHour()) >= Integer.parseInt(hourShiftList.get(0).getStart())) ||
                                (machineMoldMatchingHistory.getMatchDay().equals(toDay) && Integer.parseInt(machineMoldMatchingHistory.getMatchHour()) <= Integer.parseInt(hourShiftList.get(hourShiftList.size()-1).getEnd())))) {
                            startHour = machineMoldMatchingHistory.getMatchHour();
                        } else {
                            startHour = hourShiftList.get(0).getStart().substring(0, 2) + "00";
                        }

                        if (machineMoldMatchingHistory.getUnmatchDay() != null
                                && ((machineMoldMatchingHistory.getUnmatchDay().equals(fromDay) && Integer.parseInt(machineMoldMatchingHistory.getUnmatchHour()) >= Integer.parseInt(hourShiftList.get(0).getStart())) ||
                                (machineMoldMatchingHistory.getUnmatchDay().equals(toDay) && Integer.parseInt(machineMoldMatchingHistory.getUnmatchHour()) <= Integer.parseInt(hourShiftList.get(hourShiftList.size()-1).getEnd())))) {

                            endHour = machineMoldMatchingHistory.getUnmatchHour();
                        } else {
                            endHour = hourShiftList.get(hourShiftList.size()-1).getEnd().substring(0, 2) + "00";
                        }

                        expectedHourlyProduction.setStart(startHour);

                        expectedHourlyProduction.setEnd(endHour);

                        int wact;
                        if ("WACT".equals(oct)) {
                            wact = mold.getWeightedAverageCycleTime() == null ? (mold.getContractedCycleTime() == null ? mold.getToolmakerContractedCycleTime() : mold.getContractedCycleTime()) : mold.getWeightedAverageCycleTime().intValue();
                        } else {
                            wact = mold.getContractedCycleTime() == null ? mold.getToolmakerContractedCycleTime() : mold.getContractedCycleTime();
                        }
                        int activeCavities = mold.getMoldParts().stream().filter(m -> m.getCavity() != null && m.getCavity() > 0).mapToInt(MoldPart::getCavity).sum();
                        int expectedProducedPart = 36000 / (wact == 0 ? 1 : wact) * activeCavities;
                        expectedHourlyProduction.setExpectedProducedPart(expectedProducedPart);

                        expectedHourlyProductionList.add(expectedHourlyProduction);
                    });

                    List<OeeByShift> validBlocks = finalOeeByShifts.stream().filter(o -> o.getOee() != null).collect(Collectors.toList());
                    OeeByShift latestBlock = validBlocks.stream().filter(o -> o.getUntilInstant() != null).min(Comparator.comparing(OeeByShift::getUntilInstant)).orElse(null);
                    Instant now = Instant.now();
                    Instant until = latestBlock == null ? now : latestBlock.getUntilInstant();

                    Instant startInstant = DateUtils2.toInstant(fromTimeStr, DateUtils2.DatePattern.yyyyMMddHHmm, LocationUtils.getZoneIdByLocationId(machine.getLocationId()));
                    Instant endInstant = DateUtils2.toInstant(toTimeStr, DateUtils2.DatePattern.yyyyMMddHHmm, LocationUtils.getZoneIdByLocationId(machine.getLocationId()));
                    Double workingTime = until.compareTo(endInstant) > 0
                            ? 24D
                            : (double) Duration.between(startInstant, until).getSeconds() / 3600;
                    if (now.compareTo(endInstant) <= 0) endInstant = until;
                    Double plannedDowntime = getMachineDowntimeValueByType(machine, startInstant, endInstant, MachineAvailabilityType.PLANNED_DOWNTIME);
                    Double unPlannedDowntime = getMachineDowntimeValueByType(machine, startInstant, endInstant, MachineAvailabilityType.UNPLANNED_DOWNTIME);

                    Long totalProduced = finalOeeByShifts.stream().filter(o -> o.getPartProduced() != null).mapToLong(OeeByShift::getPartProduced).sum();
                    Long totalRejected = finalOeeByShifts.stream().filter(o -> o.getRejectedPart() != null).mapToLong(OeeByShift::getRejectedPart).sum();

                    Double fq = totalProduced == 0 ? 0 : (((totalProduced - totalRejected) / (double) totalProduced) * 100);
                    Double fa = calculateDailyFa(workingTime, plannedDowntime, unPlannedDowntime);

                    Long expectedProduction = 0L;
                    if (CollectionUtils.isNotEmpty(validBlocks)) {
                        int lastIndex = validBlocks.size() - 1;
                        Double netTime = workingTime - plannedDowntime - unPlannedDowntime;
                        expectedProduction = (long)(netTime * (long)(36000L * validBlocks.get(lastIndex).getActiveCavities() / validBlocks.get(lastIndex).getAct()));
                    }
                    Double fp = calculateDailyFp(expectedProduction, totalProduced);
                    return OeeDetail.builder()
                            .machineId(machine.getId())
                            .machineCode(machine.getMachineCode())
                            .totalPartProduced(totalProduced)
                            .rejectedPart(totalRejected)
                            .goodProduction(totalProduced - totalRejected)
                            .numberOfShift(dayShift.getNumberOfShifts())
                            .start(dayShift.getStart())
                            .end(dayShift.getEnd())
                            .fa(fa)
                            .fp(fp)
                            .fq(fq)
                            .shiftData(finalOeeByShifts)
                            .expectedHourlyProduction(expectedHourlyProductionList)
                            .build();
                }, machinePool);
                machineFutures.add(oeeDetailCompletableFuture);
            });

            machineFutures.forEach(f -> {
                try {
                    result.add(f.get());
                } catch (InterruptedException | ExecutionException e) {
                    throw new RuntimeException(e);
                }
            });
        } finally {
            machinePool.shutdown();
        }

        List<OeeDetail> sortedResult = result.stream().sorted(Comparator.comparing(OeeDetail::getMachineCode)).collect(Collectors.toList());

        return new PageImpl<>(sortedResult, pageable, machines.getTotalElements());
    }

    private Pair<Instant, Instant> getStartEndHourShift(String day, HourShift hourShift, List<HourShift> hourShifts, Machine machine) {
        int firstHourShift = Integer.parseInt(hourShifts.get(0).getStart().substring(0,2));
        int checkedHourShift = Integer.parseInt(hourShift.getStart().substring(0,2));
        String timeZone = LocationUtils.getZoneIdByLocationId(machine.getLocationId());
        String startDay = day;
        String endDay = day;
        if (firstHourShift > checkedHourShift) {
            Instant currentDay = DateUtils2.toInstant(day, DateUtils.YYYY_MM_DD_DATE_FORMAT, timeZone);
            startDay = DateUtils.getNextDay(currentDay, DateUtils.YYYY_MM_DD_DATE_FORMAT, 1);
            endDay = startDay;
        }
        if (Integer.parseInt(hourShift.getStart()) > Integer.parseInt(hourShift.getEnd())) {
            Instant currentDay = DateUtils2.toInstant(day, DateUtils.YYYY_MM_DD_DATE_FORMAT, timeZone);
            endDay = DateUtils.getNextDay(currentDay, DateUtils.YYYY_MM_DD_DATE_FORMAT, 1);
        }

        String start = startDay+hourShift.getStart().substring(0,2);
        String end = endDay+hourShift.getEnd().substring(0,2);
        return Pair.of(DateUtils2.toInstant(start, "yyyyMMddHH", timeZone), DateUtils2.toInstant(end, "yyyyMMddHH", timeZone));
    }

    private String getRealDayInShift(String day, HourShift hourShift,  List<HourShift> hourShifts) {
        if (shiftConfigService.isShiftConfigContains2Days(hourShifts)) {
            int firstHourShift = Integer.parseInt(hourShifts.get(0).getStart().substring(0,2));
            int checkedHourShift = Integer.parseInt(hourShift.getStart().substring(0,2));
            if (firstHourShift > checkedHourShift) {
                Instant currentDay = DateUtils2.toInstant(day, DateUtils.YYYY_MM_DD_DATE_FORMAT, DateUtils2.Zone.SYS);
                day = DateUtils.getNextDay(currentDay, DateUtils.YYYY_MM_DD_DATE_FORMAT, 1);
                return day;
            }
        }
        return day;
    }

    private Pair<Instant, Instant> getStartEndHourShift(List<String> hours) {
        Instant start;
        Instant end;
        if (hours.size() == 1) {
            start = DateUtils2.toInstant(hours.get(0), DateUtils2.DatePattern.yyyyMMddHH, DateUtils2.Zone.SYS);
            end = start.plus(1, ChronoUnit.HOURS);
        } else {
            start = DateUtils2.toInstant(hours.get(0), DateUtils2.DatePattern.yyyyMMddHH, DateUtils2.Zone.SYS);
            end = DateUtils2.toInstant(hours.get(hours.size() - 1), DateUtils2.DatePattern.yyyyMMddHH, DateUtils2.Zone.SYS);
        }
        return Pair.of(start, end);
    }

    private Pair<Long, Long> getStartEndDayOfHourShift(String day, HourShift hourShift, List<HourShift> hourShifts) {
        String realDayInShift = getRealDayInShift(day, hourShift, hourShifts);
        long startDateOfShift = Long.parseLong(realDayInShift.concat(hourShift.getStart()));
        long endDateOfShift = Long.parseLong(realDayInShift.concat(hourShift.getEnd()));
        if(startDateOfShift > endDateOfShift) {
            Instant currentDay = DateUtils2.toInstant(realDayInShift, DateUtils.YYYY_MM_DD_DATE_FORMAT, DateUtils2.Zone.SYS);
            realDayInShift = DateUtils.getNextDay(currentDay, DateUtils.YYYY_MM_DD_DATE_FORMAT, 1);
        }
        endDateOfShift = Long.parseLong(realDayInShift.concat(hourShift.getEnd()));
        return Pair.of(startDateOfShift, endDateOfShift);
    }

    private boolean compareDateInBetweenShiftDay(String day, HourShift hourShift, List<HourShift> hourShifts, String compareDay, String compareHour) {
        if (compareDay == null || compareHour == null) return  false;

        long compareDate = Long.parseLong(compareDay.concat(compareHour));
        Pair<Long, Long> shiftDate = getStartEndDayOfHourShift(day, hourShift, hourShifts);

        return shiftDate.getFirst() <= compareDate && shiftDate.getSecond() >= compareDate;
    }

    private OeeByShift getSingleShiftOee(HourShift hourShift, List<String> hours, Machine machine, String day,
                                         List<MachineMoldMatchingHistory> machineMoldMatchingHistoryList,
                                         List<HourShift> hourShifts,
                                         String colorCodeConfig,
                                         String latestMachineOeeHour,
                                         String oct,
                                         List<MachineDowntimeAlert> machineDowntimeAlertDataList) {

        Pair<Instant, Instant> shiftTime = getStartEndHourShift(day, hourShift, hourShifts, machine);
        List<DowntimeOeeDTO> machineDownTimeAlertShift = machineDowntimeAlertDataList.stream()
                .filter(machineDowntimeAlertData -> (machineDowntimeAlertData.getEndTime() == null && machineDowntimeAlertData.getStartTime().isBefore(shiftTime.getSecond()))
                        || (
                                (machineDowntimeAlertData.getEndTime() != null)
                                        && (
                                        (machineDowntimeAlertData.getStartTime().isBefore(shiftTime.getFirst())
                                                && machineDowntimeAlertData.getEndTime().isAfter(shiftTime.getFirst()))
                                                || (machineDowntimeAlertData.getStartTime().isBefore(shiftTime.getSecond())
                                                && machineDowntimeAlertData.getEndTime().isAfter(shiftTime.getSecond()))
                                                || (shiftTime.getFirst().isBefore(machineDowntimeAlertData.getStartTime())
                                                && shiftTime.getSecond().isAfter(machineDowntimeAlertData.getStartTime()))
                                )
                        )
                ).map( machineDowntimeAlertData -> new DowntimeOeeDTO(machineDowntimeAlertData, shiftTime, hourShift))
                .collect(Collectors.toList());
        //machine mold history
        List<MachineMoldHistoryData> machineMoldHistoryDataList = new ArrayList<>();
        try {
            machineMoldMatchingHistoryList.forEach(machineMoldMatchingHistory -> {
                if (compareDateInBetweenShiftDay(day, hourShift, hourShifts, machineMoldMatchingHistory.getMatchDay(), machineMoldMatchingHistory.getMatchHour())) {
                    MachineMoldHistoryData machineMoldHistoryData = new MachineMoldHistoryData();
                    machineMoldHistoryData.setStatus(MatchStatus.MATCHED);
                    machineMoldHistoryData.setMoldCode(machineMoldMatchingHistory.getMold().getEquipmentCode());
                    machineMoldHistoryData.setMachineCode(machineMoldMatchingHistory.getMachine().getMachineCode());
                    machineMoldHistoryData.setTime(machineMoldMatchingHistory.getMatchTime());
                    StringBuilder timeStringBuilder = new StringBuilder(machineMoldMatchingHistory.getMatchDay())
                            .append(" ")
                            .append(machineMoldMatchingHistory.getMatchHour());
                    timeStringBuilder.insert(4, "-");
                    timeStringBuilder.insert(7, "-");
                    timeStringBuilder.insert(13, ":");
                    machineMoldHistoryData.setTimeStr(timeStringBuilder.toString());
                    machineMoldHistoryDataList.add(machineMoldHistoryData);
                }

                if (compareDateInBetweenShiftDay(day, hourShift, hourShifts, machineMoldMatchingHistory.getUnmatchDay(), machineMoldMatchingHistory.getUnmatchHour())) {
                    MachineMoldHistoryData machineMoldHistoryData = new MachineMoldHistoryData();
                    machineMoldHistoryData.setStatus(MatchStatus.UNMATCHED);
                    machineMoldHistoryData.setMoldCode(machineMoldMatchingHistory.getMold().getEquipmentCode());
                    machineMoldHistoryData.setMachineCode(machineMoldMatchingHistory.getMachine().getMachineCode());
                    machineMoldHistoryData.setTime(machineMoldMatchingHistory.getUnmatchTime());
                    StringBuilder timeStringBuilder = new StringBuilder(machineMoldMatchingHistory.getUnmatchDay())
                            .append(" ")
                            .append(machineMoldMatchingHistory.getUnmatchHour());
                    timeStringBuilder.insert(4, "-");
                    timeStringBuilder.insert(7, "-");
                    timeStringBuilder.insert(13, ":");
                    machineMoldHistoryData.setTimeStr(timeStringBuilder.toString());
                    machineMoldHistoryDataList.add(machineMoldHistoryData);
                }

            });

        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
        List<MachineOee> machineOees = machineOeeRepository.findByMachineIdAndHourInAndPeriodType(machine.getId(), hours, Frequent.HOURLY);
        if (CollectionUtils.isEmpty(machineOees)) {
            return OeeByShift.builder()
                    .hourShiftId(hourShift.getId())
                    .start(hourShift.getStart())
                    .end(hourShift.getEnd())
                    .shiftNumber(hourShift.getShiftNumber())
                    .oee(null)
                    .progress(null)
                    .machineMoldHistoryData(CollectionUtils.isNotEmpty(machineMoldHistoryDataList) ? machineMoldHistoryDataList : null)
                    .downTimeOeeList(machineDownTimeAlertShift)
                    .until(null)
                    .build();
        } else {
            Optional<MachineOee> oeeOptional = machineOeeRepository.findFirstByMachineIdAndHourInAndPeriodTypeOrderByHourDesc(machine.getId(), hours, Frequent.HOURLY);
            Double progress = null;
            String until = null;
            Instant untilInstant = null;
            if (latestMachineOeeHour != null && hours.contains(latestMachineOeeHour) && oeeOptional.isPresent()) {
                MachineOee machineOee = oeeOptional.get();
                String time = machineOee.getTenMinute();
                if (time.length() == 14) {
                    untilInstant = DateUtils2.toInstant(time, DateUtils2.DatePattern.yyyyMMddHHmmss, LocationUtils.getZoneIdByLocationId(machineOee.getMachine().getLocationId()));
                }
                if (time == null) {
                    progress = 100D;
                    until = machineOee.getHour().substring(8, 10) + ":59";
                } else {
                    int tenMinNum = Integer.parseInt(time.substring(10,12));
                    progress = (tenMinNum / 60D) * 100;
                    until = time.substring(8, 10) + ":" + time.substring(10, 12);
                }
            }

//        Pair<Instant, Instant> startEndHourShift =  getStartEndHourShift(day, hourShift, hourShifts);

//            String shiftDay = getRealDayInShift(day, hourShift, hourShifts);
//
//            Pair<Integer, Integer> realShiftDate = getStartEndDayOfHourShift(day, hourShift, hourShifts);

            Double fa = machineOees.stream().mapToDouble(MachineOee::getFa).average().orElse(0);
            Double fp = machineOees.stream().mapToDouble(MachineOee::getFp).average().orElse(0);

            Long partProduced = machineOees.stream().filter(m -> m.getPartProduced() != null).mapToLong(MachineOee::getPartProduced).sum();
            Long rejectedPart = machineOees.stream().filter(m -> m.getRejectedPart() != null).mapToLong(MachineOee::getRejectedPart).sum();
            Double fq = partProduced == 0 ? 0 : (((partProduced - rejectedPart) / (double) partProduced) * 100);

            /**
             * if colorCodeConfig = OEE -> percentageForColorCode is oee
             * if colorCodeConfig = EHO -> percentageForColorCode is part produced / expected hourly production
             */

            Double percentageForColorCode = null;
            Double wact = 1D;
            Integer activeCavities = 1;
            Mold mold = machineOees.get(0).getMold();
            {
                Double fpNotExceed100 = machineOees.stream().mapToDouble(a -> {
                    if (a.getFp() > 100) return 100;
                    else return a.getFp();
                }).average().orElse(0);
                percentageForColorCode = (fa/100) * (fpNotExceed100/100) * (fq/100) * 100;
            }

            if (mold != null) {
                activeCavities = mold.getMoldParts().stream().filter(m -> m.getCavity() != null && m.getCavity() > 0).mapToInt(MoldPart::getCavity).sum();
                if ("WACT".equals(oct)) {
                    wact = mold.getWeightedAverageCycleTime() == null ? (mold.getContractedCycleTime() == null ? mold.getToolmakerContractedCycleTime() : mold.getContractedCycleTime()) : mold.getWeightedAverageCycleTime();
                } else {
                    wact = mold.getContractedCycleTime() == null ? mold.getToolmakerContractedCycleTime().doubleValue() : mold.getContractedCycleTime();
                }

                if ("EHO".equals(colorCodeConfig)) {
                    int expectedProducedPart = (int)(36000 / (wact == 0 ? 1 : wact) * activeCavities);
                    percentageForColorCode = ((double) partProduced / expectedProducedPart) * 100;
                }
            }

            Integer downtimeDuration = machineOees.stream().mapToInt(m -> {
                if (m.getDowntimeDuration() != null) {
                    return m.getDowntimeDuration();
                } else {
                    Integer workingSeconds = StringUtils.isEmpty(m.getTenMinute()) ? 3600
                            : (Integer.parseInt(m.getTenMinute().substring(10,12)) * 60 + Integer.parseInt(m.getTenMinute().substring(12,14)));
                    return m.getFa() == null || m.getFa() < 0 ? workingSeconds : (int) (m.getFa() > 100 ? 3600 : (workingSeconds - (workingSeconds * (m.getFa() / 100))));
                }
            }).sum();

            return OeeByShift.builder()
                    .hourShiftId(hourShift.getId())
                    .start(hourShift.getStart())
                    .end(hourShift.getEnd())
                    .shiftNumber(hourShift.getShiftNumber())
                    .partProduced(partProduced)
                    .rejectedPart(rejectedPart)
                    .fa(fa)
                    .fp(fp)
                    .fq(fq)
                    .oee(percentageForColorCode)
                    .progress(progress)
                    .machineMoldHistoryData(CollectionUtils.isNotEmpty(machineMoldHistoryDataList) ? machineMoldHistoryDataList : null)
                    .until(until)
                    .untilInstant(untilInstant)
                    .act(wact)
                    .activeCavities(activeCavities)
                    .downtimeDuration(downtimeDuration)
                    .downTimeOeeList(machineDownTimeAlertShift)
                    .build();
        }
    }

    private Double calculateDailyFp(List<OeeByShift> data, Long totalProduced) {
        AtomicReference<Long> expectedProduction = new AtomicReference<>(0L);
        data.forEach(d -> expectedProduction.updateAndGet(v -> v + (long) (((d.getCurrentHour() - d.getCurrentDowntime()) * 36000) / d.getAct()) * d.getActiveCavities()));
        Double fp = expectedProduction.get() == 0 ? 0D : (((double)totalProduced / expectedProduction.get()) * 100);
        return fp >= 100D ? 100D : (fp <= 0 ? 0D : fp);
    }

    private Double calculateDailyFp(Long expectedProduction, Long totalProduced) {
        Double fp = expectedProduction == 0 ? 0D : (((double)totalProduced / expectedProduction) * 100);
        return fp >= 100D ? 100D : (fp <= 0 ? 0D : fp);
    }

    private Double calculateDailyFa(List<OeeByShift> data) {
        Double totalHour = data.stream().mapToDouble(OeeByShift::getCurrentHour).sum();
        Double totalDowntime = data.stream().mapToDouble(OeeByShift::getCurrentDowntime).sum();

        return totalHour == 0 ? 0D : ((totalHour - totalDowntime) * 100 / totalHour);
    }

    private Double calculateDailyFa(Double workingTime, Double downtime) {
        Double fa = workingTime == 0 ? 0D : ((workingTime - downtime) * 100 / workingTime);
        return fa >= 100 ? 100D : (fa <= 0 ? 0D : fa);
    }

    private Double calculateDailyFa(Double workingTime, Double plannedDowntime, Double unPlannedDowntime) {
        if ((workingTime - plannedDowntime) <= 0) return 0D;
        Double fa = workingTime == 0 ? 0D : ((workingTime - plannedDowntime - unPlannedDowntime) * 100 / (workingTime - plannedDowntime));
        return fa >= 100 ? 100D : (fa <= 0 ? 0D : fa);
    }

    private List<String> getHoursOfShift(HourShift hourShift, String day, CustomBoolean switchDay) {
        List<String> result = new ArrayList<>();
        if (StringUtils.isEmpty(hourShift.getStart())&&StringUtils.isEmpty(hourShift.getEnd())){
            return null;
        }
        int start = Integer.parseInt(hourShift.getStart().substring(0,2));
        int end = Integer.parseInt(hourShift.getEnd().substring(0,2));
        Instant currentDay = DateUtils.getInstant(day, DateUtils.YYYY_MM_DD_DATE_FORMAT);
        String nextDay = DateUtils.getNextDay(currentDay, DateUtils.YYYY_MM_DD_DATE_FORMAT, 1);

        if (start > end) {
            switchDay.setValue(true);
            if (end == 0) {
                if (start == 23) {
                    return Collections.singletonList(day + 23);
                } else {
                    for (int i = start; i <= 23; i++) {
                        result.add(day + DataUtils.intNumberToDay(i));
                    }
                    return result;
                }
            } else {
                if (start == 23) {
                    result.add(day + 23);
                    for (int i = 0 ; i < end ; i++) {
                        result.add(nextDay + DataUtils.intNumberToDay(i));
                    }
                    return result;
                } else {
                    for (int i = start; i <= 23; i++) {
                        result.add(day + DataUtils.intNumberToDay(i));
                    }
                    for (int i = 0 ; i < end ; i++) {
                        result.add(nextDay + DataUtils.intNumberToDay(i));
                    }
                    return result;
                }
            }
        } else if (start == (end - 1)) {
            if (switchDay.isValue()) {
                return Collections.singletonList(nextDay + DataUtils.intNumberToDay(start));
            }
            return Collections.singletonList(day + DataUtils.intNumberToDay(start));
        } else {
            if (switchDay.isValue()) {
                for (int i = start; i < end; i++) {
                    result.add(nextDay + DataUtils.intNumberToDay(i));
                }
                return result;
            }
            for (int i = start; i < end; i++) {
                result.add(day + DataUtils.intNumberToDay(i));
            }
            return result;
        }
    }

    private Page<OeeDetail> mockDataOeeDetail(Pageable pageable) {
        List<OeeDetail> result = new ArrayList<>();
        Page<Machine> machines = machineRepository.findAll(pageable);
        machines.getContent().forEach(machine -> {
            List<OeeByShift> oeeByShifts = new ArrayList<>();
            DayShift dayShift = dayShiftRepository.getOne(10L);
            dayShift.getHourShifts().forEach(hourShift -> oeeByShifts.add(randomOeeByShift(hourShift)));
            OeeDetail oeeDetail = OeeDetail.builder()
                    .machineId(machine.getId())
                    .machineCode(machine.getMachineCode())
                    .totalPartProduced(oeeByShifts.stream().mapToLong(OeeByShift::getPartProduced).sum())
                    .numberOfShift(dayShift.getNumberOfShifts())
                    .start(dayShift.getStart())
                    .end(dayShift.getEnd())
                    .fa(100D)
                    .fp(randomByIndex(machines.getContent().indexOf(machine)))
                    .fq(randomByIndex(machines.getContent().indexOf(machine)))
                    .shiftData(oeeByShifts)
                    .build();
            result.add(oeeDetail);
        });
        return new PageImpl<>(result, pageable, machines.getTotalElements());
    }

    private Double randomByIndex(int index) {
        if (index % 2 == 0) {
            return (double)(new Random().nextInt(100 - 90) + 90);
        } else {
            if (index % 3 == 0) {
                return (double)(new Random().nextInt(80 - 70) + 70);
            } else {
                return (double)(new Random().nextInt(30 - 1) + 1);
            }
        }
    }
    private OeeByShift randomOeeByShift(HourShift hourShift) {
        return OeeByShift.builder()
                .hourShiftId(hourShift.getId())
                .shiftNumber(hourShift.getShiftNumber())
                .start(hourShift.getStart())
                .end(hourShift.getEnd())
                .partProduced((long)(new Random().nextInt(2000 - 1) + 1))
                .oee((double)(new Random().nextInt(100 - 1) + 1))
                .build();
    }

    public void updateOEEByDay_New(String day) {

        List<Machine> machines = machineRepository.findAll();

        Instant start = DateUtils.getInstant(day, DateUtils.YYYY_MM_DD_DATE_FORMAT);
        Instant end = start.plus(1, ChronoUnit.DAYS);

        List<MachineStatistics> listToUpdate = new ArrayList<>();
        machines.forEach(machine -> {
            Double fa;
            Double fp;
            Double fq;

            List<MachineDowntimeAlert> alerts = machineDowntimeAlertRepository.findByMachineInAndDowntimeOverlapped(Collections.singletonList(machine), start, end);

            //Calculate fa
            Double hw = 24D; //temporarily fixed
            Long totalPlannedDowntimeInSecond = alerts.stream()
                    .filter(a -> a.getDowntimeType().equals(MachineAvailabilityType.PLANNED_DOWNTIME))
                    .mapToLong(a -> getDowntimeInSecond(a.getStartTime(), a.getEndTime(), start, end)).sum();
            Double hp = NumberUtils.roundOffNumber((double)totalPlannedDowntimeInSecond/3600);
            Long totalUnplannedDowntimeInSecond = alerts.stream()
                    .filter(a -> a.getDowntimeType().equals(MachineAvailabilityType.UNPLANNED_DOWNTIME))
                    .mapToLong(a -> getDowntimeInSecond(a.getStartTime(), a.getEndTime(), start, end)).sum();
            Double hu = NumberUtils.roundOffNumber((double)totalUnplannedDowntimeInSecond/3600);
            Double availableTime = hw - hp - hu;
            Double plannedTime = (hw - hp) == 0 ? 1 : (hw - hp);
            fa = ((availableTime <= 0 ? 0 : availableTime) / plannedTime) * 100;

            //Calculate fp
            Optional<List<MachineMoldMatchingHistory>> optionalList = machineMoldMatchingHistoryRepository.findByMachineAndMatchDayOrUnmatchDay(machine, day);
            if (!optionalList.isPresent()) {
                Optional<MachineMoldMatchingHistory> history = machineMoldMatchingHistoryRepository.findByMachineAndDay(machine, day);
                if (history.isPresent()) {
                    fp = calculateFp(history.get().getMold().getId(), day, hw, hp, hu);
                } else {
                    if (machine.getMold() == null) {
                        fp = 0D;
                    } else {
                        fp = calculateFp(machine.getMold().getId(), day, hw, hp, hu);
                    }
                }

            } else {
                //todo calculate fp when match/unmatch within a day
                fp = calculateFpWhenMatchOrUnmatch(machine, day, hw, hp, hu);
            }

            //Calculate fq
            fq = 90D; //fixed
//            List<ProducedPart> producedParts = producedPartRepository.findByMoldIdAndDay(machine.getMold().getId(), yesterday);
//            Integer totalRejected = producedParts.stream().mapToInt(ProducedPart::getTotalRejectedAmount).sum();
//            Integer totalProduced = producedParts.stream().mapToInt(ProducedPart::getTotalProducedAmount).sum();
//            fq = (double)(totalRejected / totalProduced) * 100;

            double oee = (fa / 100 * fp / 100 * fq / 100) * 100;
        });

        machineStatisticsRepository.saveAll(listToUpdate);
    }

    private Long getDowntimeInSecond(Instant start1, Instant end1, Instant start2, Instant end2) {
        if (end1 == null) {
            if (start2.compareTo(start1) >= 0) {
                return 24L * 3600;
            } else {
                return end2.getEpochSecond() - start1.getEpochSecond();
            }
        } else {
            if (start2.compareTo(start1) > 0 && end2.compareTo(end1) < 0) {
                return 24L * 3600;
            } else if (start2.compareTo(start1) < 0 && end2.compareTo(end1) > 0) {
                return end1.getEpochSecond() - start1.getEpochSecond();
            } else if (start2.compareTo(start1) > 0 && end2.compareTo(end1) > 0) {
                return end1.getEpochSecond() - start2.getEpochSecond();
            } else {
                return end2.getEpochSecond() - start1.getEpochSecond();
            }
        }
    }

    public ApiResponse fixAvailability() {
        try {
            List<MachineStatistics> machineStatistics = machineStatisticsRepository.findAll();
            List<DowntimeItem> downtimeItemsToUpdate = new ArrayList<>();
            machineStatistics.forEach(statistics -> {
                //fix migrate downtime
                if (statistics.getPlannedDowntime() != 0 || statistics.getPlannedDowntimeMinute() != 0) {
                    List<DowntimeItem> downtimeItems = downtimeItemRepository.findByMachineStatisticsAndType(statistics, MachineAvailabilityType.PLANNED_DOWNTIME);
                    if (CollectionUtils.isEmpty(downtimeItems)) {
                        DowntimeItem item = new DowntimeItem(
                                statistics.getId(),
                                statistics,
                                MachineAvailabilityType.PLANNED_DOWNTIME,
                                0, 0,
                                statistics.getPlannedDowntime() == 24 ? 23 : statistics.getPlannedDowntime(),
                                statistics.getPlannedDowntime() == 24 ? 60 : statistics.getPlannedDowntimeMinute(),
                                statistics.getPlannedDowntimeType(),
                                statistics.getPlannedDowntimeNote());
                        downtimeItemsToUpdate.add(item);
                    } else {
                        int totalDowntimeInMainObject = statistics.getPlannedDowntime() * 60 + statistics.getPlannedDowntimeMinute();
                        int totalDowntimeInListItem = 0;
                        for (DowntimeItem item : downtimeItems) {
                            TwoObject<Integer, Integer> hourAndMinute = DateTimeUtils.subtractHourAndMinute(item.getHourFrom(), item.getMinuteFrom(), item.getHourTo(), item.getMinuteTo());
                            totalDowntimeInListItem += hourAndMinute.getLeft() * 60 + hourAndMinute.getRight();
                        }

                        if (totalDowntimeInMainObject > totalDowntimeInListItem) {
                            int downtimeToAdd = totalDowntimeInMainObject - totalDowntimeInListItem;
                            DowntimeItem item = new DowntimeItem(
                                    statistics.getId(),
                                    statistics,
                                    MachineAvailabilityType.PLANNED_DOWNTIME,
                                    0, 0,
                                    downtimeToAdd / 60,
                                    downtimeToAdd % 60,
                                    statistics.getPlannedDowntimeType() != null ? statistics.getPlannedDowntimeType() : "UNKNOWN",
                                    statistics.getPlannedDowntimeNote());
                            downtimeItemsToUpdate.add(item);
                        }
                    }
                }

                if (statistics.getUnplannedDowntime() != 0 || statistics.getUnplannedDowntimeMinute() != 0) {
                    List<DowntimeItem> downtimeItems = downtimeItemRepository.findByMachineStatisticsAndType(statistics, MachineAvailabilityType.UNPLANNED_DOWNTIME);
                    if (CollectionUtils.isEmpty(downtimeItems)) {
                        DowntimeItem item = new DowntimeItem(
                                statistics.getId(),
                                statistics,
                                MachineAvailabilityType.UNPLANNED_DOWNTIME,
                                0, 0,
                                statistics.getUnplannedDowntime() == 24 ? 23 : statistics.getUnplannedDowntime(),
                                statistics.getUnplannedDowntime() == 24 ? 60 : statistics.getUnplannedDowntimeMinute(),
                                statistics.getUnplannedDowntimeType(),
                                statistics.getUnplannedDowntimeNote());
                        downtimeItemsToUpdate.add(item);
                    } else {
                        int totalDowntimeInMainObject = statistics.getUnplannedDowntime() * 60 + statistics.getUnplannedDowntimeMinute();
                        int totalDowntimeInListItem = 0;
                        for (DowntimeItem item : downtimeItems) {
                            TwoObject<Integer, Integer> hourAndMinute = DateTimeUtils.subtractHourAndMinute(item.getHourFrom(), item.getMinuteFrom(), item.getHourTo(), item.getMinuteTo());
                            totalDowntimeInListItem += hourAndMinute.getLeft() * 60 + hourAndMinute.getRight();
                        }

                        if (totalDowntimeInMainObject > totalDowntimeInListItem) {
                            int downtimeToAdd = totalDowntimeInMainObject - totalDowntimeInListItem;
                            DowntimeItem item = new DowntimeItem(
                                    statistics.getId(),
                                    statistics,
                                    MachineAvailabilityType.UNPLANNED_DOWNTIME,
                                    0, 0,
                                    downtimeToAdd / 60,
                                    downtimeToAdd % 60,
                                    statistics.getUnplannedDowntimeType() != null ? statistics.getUnplannedDowntimeType() : "UNKNOWN",
                                    statistics.getUnplannedDowntimeNote());
                            downtimeItemsToUpdate.add(item);
                        }
                    }
                }

                //re-calculate fa
                Double hw = statistics.roundedDailyWorkHour();
                Double hp = statistics.roundedPlannedDowntime();
                Double hu = statistics.roundedUnplannedDowntime();
                double fa = ((hw - hp - hu) / ((hw - hp) == 0 ? 1 : (hw - hp))) * 100;
                statistics.setFa(fa);

                double fp = statistics.getFp() != null ? statistics.getFp() : 0;
                double fq = 90D; //fixed

                double oee = (fa / 100 * fp / 100 * fq / 100) * 100;
                statistics.setOee(oee);
            });
            machineStatisticsRepository.saveAll(machineStatistics);
            downtimeItemRepository.saveAll(downtimeItemsToUpdate);

            return ApiResponse.success(CommonMessage.OK, "machineStatistics.size(): " + machineStatistics.size() + "\ndowntimeItemsToUpdate.size():" + downtimeItemsToUpdate.size());
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    private double calculateFp(long moldId, String day, double hw, double hp, double hu) {
        List<PerformanceRawData> rawData = statisticsRepository.getPerformanceRawData(moldId, day);
        //Calculate theoretical product produced
        double availableTimeEntire = (hw - hp - hu) * 3600;
        return calculateFpForSingleMold(rawData, availableTimeEntire);
    }

    private double calculateFpForSingleMold(List<PerformanceRawData> rawData, double availableTimeEntire) {
        int totalCavity = rawData.stream().mapToInt(PerformanceRawData::getCavity).sum();
        int totalShotCount = rawData.stream().mapToInt(PerformanceRawData::getShotCount).sum();
        int actualProduct = totalCavity * totalShotCount;

        double totalCttInSeconds = rawData.stream().mapToDouble(data -> data.getAvgCtt() * data.getShotCount()).sum();
        double avgCtt = totalCttInSeconds / (totalShotCount == 0 ? 1 : totalShotCount);
        double theoreticalProductProduced = (availableTimeEntire / (avgCtt == 0 ? 1 : avgCtt)) * totalCavity;

        return (actualProduct / (theoreticalProductProduced == 0 ? 1 : theoreticalProductProduced)) * 100;
    }

    private double calculateFpForMultipleMold(Map<Long, List<PerformanceRawData>> map, double availableTimeEntire, double totalProductionTime) {

        List<PerformancePreDoneData> preDoneData = new ArrayList<>();
        map.forEach((k, v) -> {
            //Calculate actual product produced
            int totalCavity = v.stream().mapToInt(PerformanceRawData::getCavity).sum();
            int totalShotCount = v.stream().mapToInt(PerformanceRawData::getShotCount).sum();
            int actualProduct = totalCavity * totalShotCount;

            double totalCttInSeconds = v.stream().mapToDouble(data -> data.getAvgCtt() * data.getShotCount()).sum();
            double avgCtt = totalCttInSeconds / (totalShotCount == 0 ? 1 : totalShotCount);

            preDoneData.add(new PerformancePreDoneData(actualProduct, totalCttInSeconds, avgCtt, totalCavity));
        });

        long numerator = preDoneData.stream().mapToLong(PerformancePreDoneData::getActualProductProduced).sum();

        double denominator = preDoneData.stream().mapToDouble(pre -> {
            double availableTime = availableTimeEntire * (pre.getTotalCttInSeconds() / totalProductionTime);
            return (availableTime / pre.getAvgCtt()) * pre.getTotalCavity();
        }).sum();

        return (numerator / denominator) * 100;
    }

    private double calculateFpWhenMatchOrUnmatch(Machine machine, String day, double hw, double hp, double hu) {
        Map<Long, List<PerformanceRawData>> mapMoldAndData = new HashMap<>();
        //Calculate theoretical product produced
        double availableTimeEntire = (hw - hp - hu) * 3600;

        double totalActualProductionTime = 0;

        //get matched data group by mold id
        Optional<List<MachineMoldMatchingHistory>> optionalMatch = machineMoldMatchingHistoryRepository.findByMachineAndMatchDay(machine, day);
        if (optionalMatch.isPresent()) {
            for (MachineMoldMatchingHistory machineMoldMatchingHistory : optionalMatch.get()) {
                long moldId = machineMoldMatchingHistory.getMold().getId();
                Optional<Cdata> cdata = cdataRepository.findFirstByMoldIdAndDayAndLstGreaterThanEqualOrderByLstAsc(moldId, day, DateUtils.getDate(machineMoldMatchingHistory.getMatchTime(), DateUtils.DEFAULT_DATE_FORMAT));
                if (cdata.isPresent()) {
                    List<PerformanceRawData> performanceRawData = statisticsRepository.getPerformanceRawDataAfter(moldId, day, cdata.get().getLst());
                    totalActualProductionTime += calculateTotalCtt(performanceRawData);
                    if (mapMoldAndData.containsKey(moldId)) {
                        mapMoldAndData.get(moldId).addAll(performanceRawData);
                    } else {
                        mapMoldAndData.put(moldId, performanceRawData);
                    }
                }
            }
        }

        //get unmatch data group by mold id
        Optional<List<MachineMoldMatchingHistory>> optionalUnMatch = machineMoldMatchingHistoryRepository.findByMachineAndUnmatchDay(machine, day);
        if (optionalUnMatch.isPresent()) {
            for (MachineMoldMatchingHistory machineMoldMatchingHistory : optionalUnMatch.get()) {
                long moldId = machineMoldMatchingHistory.getMold().getId();
                Optional<Cdata> cdata = cdataRepository.findFirstByMoldIdAndDayAndLstLessThanEqualOrderByLstDesc(moldId, day, DateUtils.getDate(machineMoldMatchingHistory.getUnmatchTime(), DateUtils.DEFAULT_DATE_FORMAT));
                if (cdata.isPresent()) {
                    List<PerformanceRawData> performanceRawData = statisticsRepository.getPerformanceRawDataBefore(moldId, day, cdata.get().getLst());
                    totalActualProductionTime += calculateTotalCtt(performanceRawData);
                    if (mapMoldAndData.containsKey(moldId)) {
                        mapMoldAndData.get(moldId).addAll(performanceRawData);
                    } else {
                        mapMoldAndData.put(moldId, performanceRawData);
                    }
                }
            }
        }

        if (mapMoldAndData.size() == 0) {
            return 0D;
        } else if (mapMoldAndData.size() == 1) {
            return calculateFpForSingleMold(mapMoldAndData.entrySet().stream().findFirst().get().getValue(), availableTimeEntire);
        } else {
            return calculateFpForMultipleMold(mapMoldAndData, availableTimeEntire, totalActualProductionTime);
        }
    }

    private double calculateTotalCtt(List<PerformanceRawData> rawData) {
        AtomicReference<Double> totalCtt = new AtomicReference<>((double) 0);
        rawData.forEach(data -> {
            if (!StringUtils.isEmpty(data.getCttString())) {
                Map<Double, Integer> cttMap = new HashMap<>(); // key: ctt, value: shot count
                String[] splitCtt = data.getCttString().split("/");
                for (int i = 0; i < splitCtt.length - 1; i += 2) {
                    if (StringUtils.isEmpty(splitCtt[i]))
                        break;
                    cttMap.put(Double.valueOf(splitCtt[i]), Integer.valueOf(splitCtt[i + 1]));
                }
                if (cttMap.size() > 0) {
                    cttMap.forEach((k, v) -> {
                        totalCtt.updateAndGet(v1 -> v1 + Math.abs(k) * v);
                    });
                }
            }
        });
        return totalCtt.get() / 10;
    }

    public AvgOEE getOverallEquipmentEffectiveness(String start, String end, Long companyId, String line, Pageable pageable) {

        Page<DetailOEE> overAll = machineStatisticsRepository.findMachineStatisticsForOEE(start, end, line, companyId, pageable, true);

        AvgOEE avgOEE = new AvgOEE(overAll.getContent().get(0).getAvgFA(), overAll.getContent().get(0).getAvgFP(), overAll.getContent().get(0).getAvgFQ());
        avgOEE.setDetails(machineStatisticsRepository.findMachineStatisticsForOEE(start, end, line, companyId, pageable, false));
        return avgOEE;
    }

    public Page<DetailOEE> getOverallEquipmentEffectivenessDetails(String start, String end, Long companyId, String line, Pageable pageable) {
        return machineStatisticsRepository.findMachineStatisticsForOEE(start, end, line, companyId, pageable, false);
    }

    public ApiResponse checkConflict(MachineAvailabilityType type, MachineStatisticsPayload payload) {
        String from = payload.getStartDateStr().substring(0, 8);
        String to = payload.getEndDateStr().substring(0, 8);

        int firstDayHour = Integer.parseInt(payload.getStartDateStr().substring(8,10));
        int firstDayMinute = Integer.parseInt(payload.getStartDateStr().substring(10,12));
        int lastDayHour;
        int lastDayMinute;
        if (payload.getEndDateStr().endsWith("000000")) {
            lastDayHour = 23;
            lastDayMinute = 60;
        } else {
            lastDayHour = Integer.parseInt(payload.getEndDateStr().substring(8,10));
            lastDayMinute = Integer.parseInt(payload.getEndDateStr().substring(10,12));
        }

        Machine machine = machineRepository.getOne(payload.getMachineId());
        Map<String,String> resources = resourceHandler.getAllMessagesOfCurrentUser();
        if (MachineAvailabilityType.DAILY_WORKING_HOUR.equals(type)) {
            Long existDowntime = machineStatisticsRepository.existsMachineStatisticsByMachineAndDateBetweenAndPlannedDowntimeIsNotNullOrUnplannedDowntimeIsNotNull(payload.getMachineId(), from, to);
            if (existDowntime != null) {
                List<MachineStatistics> list = machineStatisticsRepository.findByMachineAndDateBetweenAndPlannedDowntimeIsNotNullOrUnplannedDowntimeIsNotNull(machine, from, to);
                List<String> days = list.stream().map(MachineStatistics::getDay).collect(Collectors.toList());
                Warning warning = new Warning(true, resources.get("warning_downtime_detected"), days);
                return ApiResponse.success(CommonMessage.OK, warning);
            } else {
                return ApiResponse.success(CommonMessage.OK, new Warning(false, null, null));
            }
        } else {
            List<String> daysBetween = DateUtils.getListStringDateBetween(payload.getStartDateStr(), payload.getEndDateStr(), DateUtils.DEFAULT_DATE_FORMAT, "yyyyMMdd");
            List<String> days = new ArrayList<>();

            if (daysBetween.size() == 1) {
                Optional<MachineStatistics> optional = machineStatisticsRepository.findByMachineAndDay(machine, daysBetween.get(0));

                TwoObject<Integer, Integer> hourAndMinute = calculateDowntimeForTheSameDay(firstDayHour, firstDayMinute, lastDayHour, lastDayMinute);
                TwoObject<Integer, Integer> downtimeFrom = new TwoObject<>(firstDayHour, firstDayMinute);
                TwoObject<Integer, Integer> downtimeTo = new TwoObject<>(lastDayHour, lastDayMinute);
                boolean conflictDay = checkDowntimeSingleDay(optional, hourAndMinute, downtimeFrom, downtimeTo);

                if (conflictDay) days.add(daysBetween.get(0));
            } else {
                daysBetween.forEach(day -> {
                    Optional<MachineStatistics> optional = machineStatisticsRepository.findByMachineAndDay(machine, day);

                    TwoObject<Integer, Integer> hourAndMinute;
                    TwoObject<Integer, Integer> downtimeFrom;
                    TwoObject<Integer, Integer> downtimeTo;
                    if (days.indexOf(day) == 0) {
                        hourAndMinute = calculateDowntimeForFirstDay(firstDayHour, firstDayMinute);
                        downtimeFrom = new TwoObject<>(firstDayHour, firstDayMinute);
                        downtimeTo = new TwoObject<>(23, 60);
                    } else if (days.indexOf(day) == (days.size() - 1)) {
                        hourAndMinute = new TwoObject<>(lastDayHour, lastDayMinute);
                        downtimeFrom = new TwoObject<>(0, 0);
                        downtimeTo = new TwoObject<>(lastDayHour, lastDayMinute);
                    } else {
                        hourAndMinute = new TwoObject<>(23, 60);
                        downtimeFrom = new TwoObject<>(0, 0);
                        downtimeTo = new TwoObject<>(lastDayHour, lastDayMinute);
                    }
                    boolean conflictDay = checkDowntimeSingleDay(optional, hourAndMinute, downtimeFrom, downtimeTo);

                    if (conflictDay) days.add(day);
                });
            }

            if (CollectionUtils.isNotEmpty(days)) return ApiResponse.success(CommonMessage.OK, new Warning(true, resources.get("warning_downtime_exceed_daily_work_hour"), days));
            else return ApiResponse.success(CommonMessage.OK, new Warning(false, null, null));

        }
    }

    private boolean checkDowntimeSingleDay(
            Optional<MachineStatistics> optional,
            TwoObject<Integer, Integer> newDowntime,
            TwoObject<Integer, Integer> downtimeFrom,
            TwoObject<Integer, Integer> downtimeTo) {
        if (optional.isPresent()) {
            MachineStatistics machineStatistics = optional.get();

            if (machineStatistics.getPlannedDowntime() == 0
                    && machineStatistics.getPlannedDowntimeMinute() == 0
                    && machineStatistics.getUnplannedDowntime() == 0
                    && machineStatistics.getUnplannedDowntimeMinute() == 0) {
                return false;
            } else {
//                TwoObject<Integer, Integer> currentDowntime = DateTimeUtils.sumHourAndMinute(
//                        machineStatistics.getPlannedDowntime(),
//                        machineStatistics.getPlannedDowntimeMinute(),
//                        machineStatistics.getUnplannedDowntime(),
//                        machineStatistics.getUnplannedDowntimeMinute());
//                int totalDowntimeToMinute = (currentDowntime.getLeft() + newDowntime.getLeft()) * 60 + currentDowntime.getRight() + newDowntime.getRight();
//                int dwhToMinute = machineStatistics.getDailyWorkingHour() * 60 + machineStatistics.getDailyWorkingHourMinute();
//
//                if (totalDowntimeToMinute > dwhToMinute) {
//                    return true;
//                } else {
                    boolean overlapped = false;
                    int newDowntimeFrom = downtimeFrom.getLeft() * 60 + downtimeFrom.getRight();
                    int newDowntimeTo = downtimeTo.getLeft() * 60 + downtimeTo.getRight();
                    List<DowntimeItem> downtimeItems = downtimeItemRepository.findByMachineStatistics(machineStatistics);
                    for (DowntimeItem downtimeItem : downtimeItems) {
                        int currentDowntimeFrom = downtimeItem.getHourFrom() * 60 + downtimeItem.getMinuteFrom();
                        int currentDowntimeTo = downtimeItem.getHourTo() * 60 + downtimeItem.getMinuteTo();
                        if (newDowntimeTo <= currentDowntimeFrom || newDowntimeFrom >= currentDowntimeTo) {
                            overlapped = false;
                        } else {
                            overlapped = true;
                            break;
                        }
                    }
                    return overlapped;
//                }
            }
        } else {
            return false;
        }
    }

    private int getEndIndex(long total, long pageSize, long offset) {
        if (total <= pageSize) {
            return (int) total;
        } else {
            if ((total - ((offset + 1) * pageSize)) <= pageSize) {
                return (int) total;
            } else {
                return (int) ((offset + 1) * pageSize);
            }
        }
    }

    private long calculateNumberOfPage(long total, long pageSize) {
        if (total <= pageSize) {
            return 1;
        } else {
            if (total % pageSize == 0) {
                return total / pageSize;
            } else {
                return (total / pageSize) + 1;
            }
        }
    }

    private List<DetailOEE> createListDetailsOEE(List<Machine> machines, Map<Long, List<MachineStatistics>> map, long daysBetween) {
        List<DetailOEE> details = new ArrayList<>();
        machines.forEach(machine -> {
            DetailOEE detail = new DetailOEE();
            if (map.containsKey(machine.getId())) {
                detail.setName(machine.getMachineCode());

                detail.setAvgFA(map.get(machine.getId()).stream().mapToDouble(MachineStatistics::getFa).sum() / daysBetween);
                detail.setAvgFP(map.get(machine.getId()).stream().mapToDouble(MachineStatistics::getFp).sum() / daysBetween);
                detail.setAvgFQ(map.get(machine.getId()).stream().mapToDouble(MachineStatistics::getFq).sum() / daysBetween);

                detail.setAvgOEE(map.get(machine.getId()).stream().mapToDouble(MachineStatistics::getOee).sum() / daysBetween);
            } else {
                detail.setName(machine.getMachineCode());
                detail.setAvgFA(0);
                detail.setAvgFP(0);
                detail.setAvgFQ(0);
                detail.setAvgOEE(0);
            }
            details.add(detail);
        });

        return details;
    }

    public List<RiskLevel> updateRiskLevel(List<RiskLevel> riskLevels) {
        List<RiskLevel> listToUpdate = new ArrayList<>();
        riskLevels.forEach(riskLevel -> {
            Optional<RiskLevel> r = riskLevelRepository.findByName(riskLevel.getName());
            if (r.isPresent()) {
                RiskLevel rl = r.get();
                rl.setPercent(riskLevel.getPercent());
                listToUpdate.add(rl);
            } else {
                RiskLevel rl = new RiskLevel();
                rl.setName(riskLevel.getName());
                rl.setPercent(riskLevel.getPercent());
                listToUpdate.add(rl);
            }
        });
        riskLevelRepository.saveAll(listToUpdate);
        return listToUpdate;
    }

    public List<RiskLevel> getAllRiskLevel() {
        List<RiskLevel> list = riskLevelRepository.findAll();
        if (CollectionUtils.isEmpty(list)) {
            list = new ArrayList<>();
            list.add(new RiskLevel(null, PriorityType.LOW, 100));
            list.add(new RiskLevel(null, PriorityType.MEDIUM, 80));
            list.add(new RiskLevel(null, PriorityType.HIGH, 50));

            riskLevelRepository.saveAll(list);
            return list;
        } else {
            return list;
        }
    }

    @Transactional
    public void bindData(MachineStatistics machineStatistics,
                         MachineStatisticsPayload payload,
                         Machine machine, String day,
                         MachineAvailabilityType type,
                         TwoObject<Integer, Integer> hourAndMinute, //downtime or daily work hour
                         TwoObject<Integer, Integer> downtimeFrom,
                         TwoObject<Integer, Integer> downtimeTo,
                         Boolean overwrite) {

        String dayWithDefaultFormatted = day + "000000";
        machineStatistics.setMachine(machine);
        machineStatistics.setDay(day);
        machineStatistics.setDate(DateUtils.getInstant(dayWithDefaultFormatted, DateUtils.DEFAULT_DATE_FORMAT));
        switch (type) {
            case DAILY_WORKING_HOUR:
                machineStatistics.setDailyWorkingHour(payload.getDailyWorkingHour());
                machineStatistics.setDailyWorkingHourMinute(0);
                machineStatistics.setDailyWorkingHourNote(payload.getDailyWorkingHourNote());
                machineStatistics.setDailyWorkingHourEditedAt(Instant.now());
                machineStatisticsRepository.save(machineStatistics);
                deleteOldDowntimeData(machineStatistics);
                break;
            case PLANNED_DOWNTIME:
                if (overwrite) {
                    deleteOldDowntimeData(machineStatistics);

                    setNewDowntimeByType(machineStatistics, MachineAvailabilityType.PLANNED_DOWNTIME, hourAndMinute);

                    overwriteDailyWorkHourIfNeeded(machineStatistics, hourAndMinute);
                } else {
                    TwoObject<Integer, Integer> newDowntime = DateTimeUtils.sumHourAndMinute(
                            machineStatistics.getPlannedDowntime(),
                            machineStatistics.getPlannedDowntimeMinute(),
                            hourAndMinute.getLeft(),
                            hourAndMinute.getRight());

                    setNewDowntimeByType(machineStatistics, MachineAvailabilityType.PLANNED_DOWNTIME, newDowntime);
                    TwoObject<Integer, Integer> currentDowntime = DateTimeUtils.sumHourAndMinute(
                            machineStatistics.getPlannedDowntime(),
                            machineStatistics.getPlannedDowntimeMinute(),
                            machineStatistics.getUnplannedDowntime(),
                            machineStatistics.getUnplannedDowntimeMinute());
                    overwriteDailyWorkHourIfNeeded(machineStatistics, currentDowntime);
                }

                machineStatisticsRepository.save(machineStatistics);
                // save downtime item
                DowntimeItem plannedItem = new DowntimeItem(
                        machineStatistics.getId(),
                        machineStatistics,
                        MachineAvailabilityType.PLANNED_DOWNTIME,
                        downtimeFrom.getLeft(),
                        downtimeFrom.getRight(),
                        downtimeTo.getLeft(),
                        downtimeTo.getRight(),
                        payload.getPlannedDowntimeType(),
                        payload.getPlannedDowntimeNote()
                );
                downtimeItemRepository.save(plannedItem);
                break;
            case UNPLANNED_DOWNTIME:
                if (overwrite) {
                    deleteOldDowntimeData(machineStatistics);

                    setNewDowntimeByType(machineStatistics, MachineAvailabilityType.UNPLANNED_DOWNTIME, hourAndMinute);

                    overwriteDailyWorkHourIfNeeded(machineStatistics, hourAndMinute);
                } else {
                    TwoObject<Integer, Integer> newDowntime = DateTimeUtils.sumHourAndMinute(
                            machineStatistics.getUnplannedDowntime(),
                            machineStatistics.getUnplannedDowntimeMinute(),
                            hourAndMinute.getLeft(),
                            hourAndMinute.getRight());

                    setNewDowntimeByType(machineStatistics, MachineAvailabilityType.UNPLANNED_DOWNTIME, newDowntime);
                    TwoObject<Integer, Integer> currentDowntime = DateTimeUtils.sumHourAndMinute(
                            machineStatistics.getPlannedDowntime(),
                            machineStatistics.getPlannedDowntimeMinute(),
                            machineStatistics.getUnplannedDowntime(),
                            machineStatistics.getUnplannedDowntimeMinute());
                    overwriteDailyWorkHourIfNeeded(machineStatistics, currentDowntime);
                }

                machineStatisticsRepository.save(machineStatistics);
                // save downtime item
                DowntimeItem unplannedItem = new DowntimeItem(
                        machineStatistics.getId(),
                        machineStatistics,
                        MachineAvailabilityType.UNPLANNED_DOWNTIME,
                        downtimeFrom.getLeft(),
                        downtimeFrom.getRight(),
                        downtimeTo.getLeft(),
                        downtimeTo.getRight(),
                        payload.getUnplannedDowntimeType(),
                        payload.getUnplannedDowntimeNote()
                );
                downtimeItemRepository.save(unplannedItem);
                break;
            default:
                break;
        }
    }

    private TwoObject<Integer, Integer> calculateDowntimeForFirstDay(int hour, int minute) {
        return DateTimeUtils.subtractHourAndMinute(hour, minute, 23, 60);
    }

    private TwoObject<Integer, Integer> calculateDowntimeForTheSameDay(int startHour, int startMinute, int endHour, int endMinute) {
        return DateTimeUtils.subtractHourAndMinute(startHour, startMinute, endHour, endMinute);
    }

    private void overwriteDailyWorkHourIfNeeded(MachineStatistics machineStatistics, TwoObject<Integer, Integer> downtime) {
        int dwhToMinute = machineStatistics.getDailyWorkingHour() * 60 + machineStatistics.getDailyWorkingHourMinute();
        int downtimeToMinute = downtime.getLeft() * 60 + downtime.getRight();
        if (dwhToMinute < downtimeToMinute) {
            TwoObject<Integer, Integer> formattedDwh = DateTimeUtils.sumHourAndMinute(downtime.getLeft(), downtime.getRight(), 0, 0);
            machineStatistics.setDailyWorkingHour(formattedDwh.getLeft());
            machineStatistics.setDailyWorkingHourMinute(formattedDwh.getRight());
            machineStatistics.setDailyWorkingHourEditedAt(Instant.now());
        }
    }

    private void setNewDowntimeByType(MachineStatistics machineStatistics, MachineAvailabilityType type, TwoObject<Integer, Integer> downtime) {
        if (type.equals(MachineAvailabilityType.PLANNED_DOWNTIME)) {
            machineStatistics.setPlannedDowntime(downtime.getLeft());
            machineStatistics.setPlannedDowntimeMinute(downtime.getRight());
        }
        if (type.equals(MachineAvailabilityType.UNPLANNED_DOWNTIME)) {
            machineStatistics.setUnplannedDowntime(downtime.getLeft());
            machineStatistics.setUnplannedDowntimeMinute(downtime.getRight());
        }
    }


    @Transactional
    public void deleteOldDowntimeData(MachineStatistics machineStatistics) {
        machineStatistics.setPlannedDowntime(null);
        machineStatistics.setPlannedDowntimeMinute(null);
        machineStatistics.setPlannedDowntimeType(null);
        machineStatistics.setPlannedDowntimeNote(null);
        machineStatistics.setUnplannedDowntime(null);
        machineStatistics.setUnplannedDowntimeMinute(null);
        machineStatistics.setUnplannedDowntimeType(null);
        machineStatistics.setUnplannedDowntimeNote(null);
        machineStatisticsRepository.save(machineStatistics);

        downtimeItemRepository.deleteByMachineStatistics(machineStatistics);
    }

    private void deleteOldDowntimeDataByType(MachineStatistics machineStatistics, MachineAvailabilityType type) {
        if (type.equals(MachineAvailabilityType.UNPLANNED_DOWNTIME)) {
            machineStatistics.setUnplannedDowntime(null);
            machineStatistics.setUnplannedDowntimeType(null);
            machineStatistics.setUnplannedDowntimeNote(null);
        }
        if (type.equals(MachineAvailabilityType.PLANNED_DOWNTIME)) {
            machineStatistics.setPlannedDowntime(null);
            machineStatistics.setPlannedDowntimeType(null);
            machineStatistics.setPlannedDowntimeNote(null);
        }
        downtimeItemRepository.deleteByMachineStatisticsAndType(machineStatistics, type);
    }

    public ApiResponse migrateOldDowntimeData() {
        try {
            List<MachineStatistics> machineStatistics = machineStatisticsRepository.findAll();
            List<DowntimeItem> items = new ArrayList<>();
            machineStatistics.forEach(m -> {
                if (m.getPlannedDowntime() != null && m.getPlannedDowntime() != 0) {
                    DowntimeItem item = new DowntimeItem(
                            m.getId(),
                            m,
                            MachineAvailabilityType.PLANNED_DOWNTIME,
                            0, 0,
                            m.getPlannedDowntime() == 24 ? 23 : m.getPlannedDowntime(),
                            m.getPlannedDowntime() == 24 ? 60 : 0,
                            m.getPlannedDowntimeType(),
                            m.getPlannedDowntimeNote());
                    items.add(item);
                }
                if (m.getUnplannedDowntime() != null && m.getUnplannedDowntime() != 0) {
                    DowntimeItem item = new DowntimeItem(
                            m.getId(),
                            m,
                            MachineAvailabilityType.UNPLANNED_DOWNTIME,
                            0, 0,
                            m.getUnplannedDowntime() == 24 ? 23 : m.getUnplannedDowntime(),
                            m.getUnplannedDowntime() == 24 ? 60 : 0,
                            m.getUnplannedDowntimeType(),
                            m.getUnplannedDowntimeNote());
                    items.add(item);
                }
            });

            downtimeItemRepository.saveAll(items);
            return ApiResponse.success(CommonMessage.OK, items.size());
        } catch (Exception e) {
            return ApiResponse.error(CommonMessage.SOMETHING_WENT_WRONG);
        }
    }

    public ApiResponse getOeeOverview_New(OeePayload oeePayload){
        try {
            OeeOverview result = new OeeOverview();

            Map<String, Object> option = BeanUtils.get(OptionService.class).getFieldValues(ConfigCategory.OPTIMAL_CYCLE_TIME);
            String oct;
            if (option.containsKey("strategy")) {
                oct = String.valueOf(option.get("strategy"));
            } else {
                oct = "WACT";
            }

            MachinePayload machinePayload = new MachinePayload();
            machinePayload.setStatus("enabled");
            machinePayload.setIds(oeePayload.getMachineIds());
            machinePayload.setLocationIds(oeePayload.getLocationIds());

            //count machine
            Long machineCount = machineRepository.count(machinePayload.getPredicate());
            result.setMachineCount(machineCount);

            machinePayload.setMatchedWithTooling(true);
            List<Machine> machinesMatched = (List<Machine>) machineRepository.findAll(machinePayload.getPredicate());
            Map<Long, Pair<String, String>> mapLocationAndStartEndHour = getMapLocationAndStartEndHour(oeePayload);
            Map<Long, Pair<String, String>> mapLocationAndPreviousStartEndHour = getMapLocationAndPreviousStartEndHour(oeePayload);

            AtomicReference<Double> currentWorkingTime = new AtomicReference<>(0D);
            AtomicReference<Double> previousWorkingTime = new AtomicReference<>(0D);
            AtomicReference<Double> currentDowntime = new AtomicReference<>(0D);
            AtomicReference<Double> currentPlannedDowntime = new AtomicReference<>(0D);
            AtomicReference<Double> currentUnPlannedDowntime = new AtomicReference<>(0D);
            AtomicReference<Double> previousDowntime = new AtomicReference<>(0D);
            AtomicReference<Long> expectedProduction = new AtomicReference<>(0L);
            AtomicReference<Long> produced= new AtomicReference<>(0L);
            List<OeeByShift> currentOeeData = new ArrayList<>();
            List<OeeByShift> previousOeeData = new ArrayList<>();
            machinesMatched.forEach(machine -> {
                //current
                Pair<String, String> startEndHour = mapLocationAndStartEndHour.get(machine.getLocationId());
                Instant now = Instant.now();
                Instant startInstant = DateUtils2.toInstant(startEndHour.getFirst(), DateUtils2.DatePattern.yyyyMMddHH, LocationUtils.getZoneIdByLocationId(machine.getLocationId()));
                Instant endInstant = DateUtils2.toInstant(startEndHour.getSecond(), DateUtils2.DatePattern.yyyyMMddHH, LocationUtils.getZoneIdByLocationId(machine.getLocationId()));
                String latestMachineOeeHour = machineOeeRepository.findFirstHourByMachineIdAndPeriodTypeOrderByHourDesc(machine.getId());
                String latestTime = machineOeeRepository.findFirstTimeByMachineIdAndPeriodTypeOrderByHourDesc(machine.getId());
                Instant latestInstant = latestTime == null ? now : DateUtils2.toInstant(latestTime, DateUtils2.DatePattern.yyyyMMddHHmmss, LocationUtils.getZoneIdByLocationId(machine.getLocationId()));
                if (!DateUtils.inRange(latestInstant, startInstant, endInstant)) {
                    latestInstant = now;
                }
                boolean isFutureDay = latestInstant.compareTo(startInstant) < 0;

                Double thisWorkingTime;
                if (now.compareTo(endInstant) >= 0) {
                    thisWorkingTime = 24D;
                } else if (isFutureDay) {
                    thisWorkingTime = 0D;
                } else {
                    thisWorkingTime = (double)Duration.between(startInstant, latestInstant).getSeconds() / 3600;
                    endInstant = latestInstant;
                }

                currentWorkingTime.updateAndGet(v -> v + thisWorkingTime);

                OeePayload payload = new OeePayload();
                payload.setMachineIds(Collections.singletonList(machine.getId()));
                payload.setStart(startEndHour.getFirst());
                payload.setEnd(startEndHour.getSecond());
                List<OeeByShift> thisOeeData = processUntil(machineOeeRepository.findAllOeeDataByHour(payload), latestMachineOeeHour);
                Long producedOfEachMachine = thisOeeData.stream().filter(o -> o.getPartProduced() != null).mapToLong(OeeByShift::getPartProduced).sum();
                currentOeeData.addAll(thisOeeData);

                Double thisDowntime;
                Double thisPlannedDowntime;
                Double thisUnPlannedDowntime;
                if (!isFutureDay) {
                    Instant finalEndInstant = endInstant;
                    thisDowntime = getMachineDowntimeValue(machine, startInstant, finalEndInstant);
                    thisPlannedDowntime = getMachineDowntimeValueByType(machine, startInstant, finalEndInstant, MachineAvailabilityType.PLANNED_DOWNTIME);
                    thisUnPlannedDowntime = getMachineDowntimeValueByType(machine, startInstant, finalEndInstant, MachineAvailabilityType.UNPLANNED_DOWNTIME);
                    currentDowntime.updateAndGet(v -> v + thisDowntime);
                    currentPlannedDowntime.updateAndGet(v -> v + thisPlannedDowntime);
                    currentUnPlannedDowntime.updateAndGet(v -> v + thisUnPlannedDowntime);
                } else {
                    thisDowntime = 0D;
                    thisPlannedDowntime = 0D;
                    thisUnPlannedDowntime = 0D;
                }

                if (CollectionUtils.isNotEmpty(thisOeeData)) {
                    Double wact;
                    Integer activeCavities;
                    Mold mold = thisOeeData.get(thisOeeData.size() - 1).getMold();
                    if (mold != null) {
                        activeCavities = mold.getMoldParts().stream().filter(m -> m.getCavity() != null && m.getCavity() > 0).mapToInt(MoldPart::getCavity).sum();
                        if ("WACT".equals(oct)) {
                            wact = mold.getWeightedAverageCycleTime() == null ? (mold.getContractedCycleTime() == null ? mold.getToolmakerContractedCycleTime() : mold.getContractedCycleTime()) : mold.getWeightedAverageCycleTime();
                        } else {
                            wact = mold.getContractedCycleTime() == null ? mold.getToolmakerContractedCycleTime().doubleValue() : mold.getContractedCycleTime();
                        }
                    } else {
                        wact = 1D;
                        activeCavities = 1;
                    }
                    double netTime = thisWorkingTime - thisDowntime;
                    long expectedOfEachMachine = (long) (netTime * (long)((36000 * activeCavities) / wact));
                    expectedProduction.updateAndGet(v -> v + expectedOfEachMachine);
                    produced.updateAndGet(v -> v + (producedOfEachMachine > expectedOfEachMachine ? expectedOfEachMachine : producedOfEachMachine));
                }


                //previous
                Pair<String, String> previousStartEndHour = mapLocationAndPreviousStartEndHour.get(machine.getLocationId());
                Instant previousStartInstant = DateUtils2.toInstant(previousStartEndHour.getFirst(), DateUtils2.DatePattern.yyyyMMddHH, LocationUtils.getZoneIdByLocationId(machine.getLocationId()));
                Instant previousEndInstant = DateUtils2.toInstant(previousStartEndHour.getSecond(), DateUtils2.DatePattern.yyyyMMddHH, LocationUtils.getZoneIdByLocationId(machine.getLocationId()));

                previousWorkingTime.updateAndGet(v -> v + 24D);
                OeePayload previousPayload = new OeePayload();
                previousPayload.setMachineIds(Collections.singletonList(machine.getId()));
                previousPayload.setStart(previousStartEndHour.getFirst());
                previousPayload.setEnd(previousStartEndHour.getSecond());
                previousOeeData.addAll(machineOeeRepository.findAllOeeDataByHour(previousPayload));
                previousDowntime.updateAndGet(v -> v + getMachineDowntimeValue(machine, previousStartInstant, previousEndInstant));
            });
            List<OeeByShift> allOeeData = new ArrayList<>(currentOeeData);
            allOeeData.addAll(previousOeeData);
            fillActAndCavityData(allOeeData, oct);

            result.setMachineDowntimeValue(NumberUtils.roundOffNumber(currentDowntime.get()));
            Double machineDowntimePercentage = getTrend(currentDowntime.get(), previousDowntime.get());
            result.setMachineDowntimePercentage(NumberUtils.roundToOneDecimalDigit(machineDowntimePercentage));

            //get part produced trend
            Long totalProduced = currentOeeData.stream().filter(o -> o.getPartProduced() != null).mapToLong(OeeByShift::getPartProduced).sum();
            result.setPartProducedValue(totalProduced);
            Long previousTotalProduced = previousOeeData.stream().filter(o -> o.getPartProduced() != null).mapToLong(OeeByShift::getPartProduced).sum();
            Double partProducedPercentage = getTrend(totalProduced.doubleValue(), previousTotalProduced.doubleValue());
            result.setPartProducedPercentage(NumberUtils.roundToOneDecimalDigit(partProducedPercentage));

            //get reject rate trend
            Long totalRejected = currentOeeData.stream().filter(o -> o.getRejectedPart() != null).mapToLong(OeeByShift::getRejectedPart).sum();
            Double rejectRate;
            if (totalProduced == 0) {
                rejectRate = 0D;
                result.setQuality(0D);
            } else {
                rejectRate = ((double)totalRejected / totalProduced) * 100;
            }
            result.setRejectRateValue(NumberUtils.roundToOneDecimalDigit(rejectRate));

            //calculate previous reject rate
            Long previousTotalRejected = previousOeeData.stream().filter(o -> o.getRejectedPart() != null).mapToLong(OeeByShift::getRejectedPart).sum();
            Double previousRejectRate;
            if (previousTotalProduced == 0) {
                previousRejectRate = 0D;
            } else {
                previousRejectRate = ((double)previousTotalRejected / previousTotalProduced) * 100;
            }

            Double rejectRatePercentage = getTrend(rejectRate, previousRejectRate);
            result.setRejectRatePercentage(NumberUtils.roundToOneDecimalDigit(rejectRatePercentage));

            // oee data
            result.setAvailability(NumberUtils.roundToOneDecimalDigit(calculateDailyFa(currentWorkingTime.get(), currentPlannedDowntime.get(), currentUnPlannedDowntime.get())));
            result.setPerformance(NumberUtils.roundToOneDecimalDigit(calculateDailyFp(expectedProduction.get(), produced.get())));
            result.setQuality(NumberUtils.roundToOneDecimalDigit(totalProduced == 0 ? 0D : (100D - rejectRate)));

            return ApiResponse.success(CommonMessage.OK, result);
        } catch (Exception e) {
            e.printStackTrace();
            return ApiResponse.error();
        }
    }

    private void fillActAndCavityData(List<OeeByShift> data, String oct) {
        data.forEach(d -> {
            Mold mold = d.getMold();
            if (mold != null) {
                d.setActiveCavities(mold.getMoldParts().stream().filter(m -> m.getCavity() != null && m.getCavity() > 0).mapToInt(MoldPart::getCavity).sum());
                Double wact;
                if ("WACT".equals(oct)) {
                    wact = mold.getWeightedAverageCycleTime() == null ? (mold.getContractedCycleTime() == null ? mold.getToolmakerContractedCycleTime() : mold.getContractedCycleTime()) : mold.getWeightedAverageCycleTime();
                } else {
                    wact = mold.getContractedCycleTime() == null ? mold.getToolmakerContractedCycleTime().doubleValue() : mold.getContractedCycleTime();
                }
                d.setAct(wact);
            }
        });
    }

    private List<OeeByShift> processUntil(List<OeeByShift> data, String lastHour) {
        data.forEach(d -> {
            if (d.getHour().equals(lastHour)) {
                if (StringUtils.isEmpty(d.getTenMinute())) {
                    d.setUntil(lastHour.substring(8, 10) + ":59");
                } else {
                    d.setUntil(d.getTenMinute().substring(8, 10) + ":" + d.getTenMinute().substring(10, 12));
                }
            }
        });

        return data;
    }

    private Pair<List<ProducedPart>, List<ProducedPart>> getProducedPart(String startHour, String endHour, String previousStartHour, String previousEndHour, List<Long> moldIds) {
        RejectedPartPayload rejectedPartPayload = new RejectedPartPayload();
        rejectedPartPayload.setFrequent(Frequent.HOURLY);
        rejectedPartPayload.setStart(startHour);
        rejectedPartPayload.setEnd(endHour);
        rejectedPartPayload.setMoldIds(moldIds);
        rejectedPartPayload.setForOEE(true);
        List<ProducedPart> producedParts = (List<ProducedPart>) producedPartRepository.findAll(rejectedPartPayload.getPredicate());

        rejectedPartPayload.setStart(previousStartHour);
        rejectedPartPayload.setEnd(previousEndHour);
        List<ProducedPart> previousProducedParts = (List<ProducedPart>) producedPartRepository.findAll(rejectedPartPayload.getPredicate());

        return Pair.of(producedParts, previousProducedParts);
    }

    private Map<Long, Pair<String, String>> getMapLocationAndPreviousStartEndHour(OeePayload oeePayload) {
        Map<Long, Pair<String, String>> map = new HashMap<>();
        if (CollectionUtils.isNotEmpty(oeePayload.getLocationIds()))
            oeePayload.getLocationIds().forEach(id -> {
                Pair<String, String> previousPeriod =  getPreviousPeriod(id, oeePayload.getStart(), oeePayload.getEnd());
                map.put(id, shiftConfigService.getStartEndByHourShift(id, previousPeriod.getFirst(), previousPeriod.getSecond()));
            });
        else
            locationRepository.findAll().stream().map(Location::getId).forEach(id -> {
                Pair<String, String> previousPeriod =  getPreviousPeriod(id, oeePayload.getStart(), oeePayload.getEnd());
                map.put(id, shiftConfigService.getStartEndByHourShift(id, previousPeriod.getFirst(), previousPeriod.getSecond()));
            });
        return map;
    }

    private Map<Long, Pair<String, String>> getMapLocationAndStartEndHour(OeePayload oeePayload) {
        Map<Long, Pair<String, String>> map = new HashMap<>();
        if (CollectionUtils.isNotEmpty(oeePayload.getLocationIds()))
            oeePayload.getLocationIds().forEach(id -> map.put(id, shiftConfigService.getStartEndByHourShift(id, oeePayload.getStart(), oeePayload.getEnd())));
        else
            locationRepository.findAll().stream().map(Location::getId).forEach(id -> map.put(id, shiftConfigService.getStartEndByHourShift(id, oeePayload.getStart(), oeePayload.getEnd())));
        return map;
    }

    private Double getTrend(Double current, Double previous) {
        if (current == 0 && previous == 0) {
            return 0D;
        } else if (current != 0 && previous == 0) {
            return 100D;
        } else if (current == 0) {
            return -100D;
        } else {
            return ((current / previous) * 100) - 100;
        }
    }

    private Duration getDurationBetween(Instant start, Instant end) {
        if (end.compareTo(start) > 0) {
                return Duration.between(start, end);
        } else {
            return Duration.of(0, ChronoUnit.MILLIS);
        }
    }

    public Double getMachineDowntimeValue(Machine machine, Instant start, Instant end) {
        List<MachineDowntimeAlert> alerts = machineDowntimeAlertRepository.findByMachineInAndDowntimeOverlapped(Collections.singletonList(machine), start, end);
        return getTotalDowntime(alerts, start, end);
    }

    public Double getMachineDowntimeValueByType(Machine machine, Instant start, Instant end, MachineAvailabilityType type) {
        List<MachineDowntimeAlert> alerts = machineDowntimeAlertRepository.findByMachineInAndDowntimeTypeAndDowntimeOverlapped(Collections.singletonList(machine), start, end, type);
        return getTotalDowntime(alerts, start, end);
    }

    private Double getTotalDowntime(List<MachineDowntimeAlert> alerts, Instant start, Instant end) {
        AtomicReference<Double> totalDowntimeHour = new AtomicReference<>(0D);
        alerts.forEach(alert -> {
            Duration timeElapsed;
            if (alert.getEndTime() ==  null) {
                if (alert.getStartTime().compareTo(start) <= 0) {
                    timeElapsed = Duration.between(start, end);
                } else {
                    timeElapsed = Duration.between(alert.getStartTime(), end);
                }
            } else {
                if (alert.getStartTime().compareTo(start) <= 0 && alert.getEndTime().compareTo(end) >= 0) {
                    timeElapsed = Duration.between(start, end);
                } else if (alert.getStartTime().compareTo(start) >= 0 && alert.getEndTime().compareTo(end) <= 0) {
                    timeElapsed = Duration.between(alert.getStartTime(), alert.getEndTime());
                } else if (alert.getStartTime().compareTo(start) <= 0 && alert.getEndTime().compareTo(end) <= 0) {
                    timeElapsed = Duration.between(start, alert.getEndTime());
                } else if (alert.getStartTime().compareTo(start) >= 0 && alert.getEndTime().compareTo(end) >= 0) {
                    timeElapsed = Duration.between(alert.getStartTime(), end);
                } else {
                    timeElapsed = Duration.ofSeconds(0);
                }
            }
            Double roundedHour = (double) timeElapsed.getSeconds()/3600;
            totalDowntimeHour.updateAndGet(v -> v + roundedHour);
        });

        return totalDowntimeHour.get();
    }

    private Pair<String, String> getPreviousPeriod(Long locationId, String start, String end) {
        Instant startTime = DateUtils2.toInstant(start, DateUtils2.DatePattern.yyyyMMdd, LocationUtils.getZoneIdByLocationId(locationId));
        Instant endTime = DateUtils2.toInstant(end, DateUtils2.DatePattern.yyyyMMdd, LocationUtils.getZoneIdByLocationId(locationId));
//        Instant startTime = DateUtils.getInstant(start, DateUtils.YYYY_MM_DD_DATE_FORMAT);
//        Instant endTime = DateUtils.getInstant(end, DateUtils.YYYY_MM_DD_DATE_FORMAT);
        Duration timeElapsed = Duration.between(startTime, endTime);

        Instant newStartTime = startTime.minus(timeElapsed.getSeconds(), ChronoUnit.SECONDS);

        if (start.equals(end)) {
            newStartTime = startTime.minus(24 * 3600, ChronoUnit.SECONDS);
            return Pair.of(DateUtils2.format(newStartTime, DateUtils2.DatePattern.yyyyMMdd, LocationUtils.getZoneIdByLocationId(locationId)),
                    DateUtils2.format(newStartTime, DateUtils2.DatePattern.yyyyMMdd, LocationUtils.getZoneIdByLocationId(locationId)));
        }
        return Pair.of(DateUtils2.format(newStartTime, DateUtils2.DatePattern.yyyyMMdd, LocationUtils.getZoneIdByLocationId(locationId)), start);
    }

    public ApiResponse coverOldData() {
        try {
            List<Machine> machines = machineRepository.findMachineMatchedWithTooling();
            List<Mold> molds = machines.stream().map(Machine::getMold).collect(Collectors.toList());

            molds.forEach(mold -> {
                Machine machine = mold.getMachine();
                List<Statistics> statisticsList = statisticsRepository.findByMoldIdOrderByHourAsc(mold.getId());
                List<MachineOee> oees = new ArrayList<>();
                int activeCavities = mold.getMoldParts().stream().filter(m -> m.getCavity() != null && m.getCavity() > 0).mapToInt(MoldPart::getCavity).sum();

                for (Statistics statistics : statisticsList) {
                    // calculate oee
                    Optional<List<MachineOee>> optional = machineOeeRepository.findByMachineAndHourAndPeriodType(machine, statistics.getHour(), Frequent.HOURLY);
                    if (!optional.isPresent()) {
                        MachineOee machineOee = MachineOee.builder()
                                .machine(machine)
                                .machineId(machine.getId())
                                .day(statistics.getDay())
                                .hour(statistics.getHour())
                                .partProduced(statistics.getShotCount() != null ? (long)statistics.getShotCount()*activeCavities : 0)
                                .partProducedVal(statistics.getShotCountVal() != null ? (long)statistics.getShotCountVal()*activeCavities : 0)
                                .build();
                        machineOee.setFa(100D);
                        machineOee.setFq(100D);
                        Long availableTime = 3600L; //1 hour to seconds
                        Double fp = (statistics.getShotCount() / (availableTime / (statistics.getCt()*0.1))) * 100;
                        machineOee.setFp(fp > 100 ? 100D : fp);
                        oees.add(machineOee);
                    }
                }


                //save data
                machineOeeRepository.saveAll(oees);
            });

            return ApiResponse.success();
        } catch (Exception e) {
            e.printStackTrace();
            return ApiResponse.error();
        }

    }

    private Map<HourShift, List<String>> mapHourShiftToListHours (List<HourShift> hourShifts, String day, CustomBoolean switchDay) {
        Map<HourShift, List<String>> map = new HashMap<>();
        hourShifts.forEach(hourShift -> {
            List<String> hours = getHoursOfShift(hourShift, day, switchDay);
            map.put(hourShift, hours);
        });
        return map;
    }

    public ApiResponse migrateMachineRejectedPart() {
        List<MachineOee> machineOees = (List<MachineOee>) machineOeeRepository.findAll(QMachineOee.machineOee.periodType.eq(Frequent.HOURLY));
        machineOees.forEach(m -> {
            if (m.getMoldId() != null) {
                List<ProducedPart> producedParts = producedPartRepository.findByMoldIdInAndFrequentAndHour(Collections.singletonList(m.getMoldId()), Frequent.HOURLY, m.getHour());
                if (CollectionUtils.isNotEmpty(producedParts)) {
                    m.setRejectedPart(producedParts.get(0).getTotalRejectedAmount() != null ? producedParts.get(0).getTotalRejectedAmount().longValue() : 0);
                }
            }
        });
        return ApiResponse.success();
    }

    public ApiResponse migrateOldMachineHistoryTime(){
        List<MachineMoldMatchingHistory> machineMatchingHistory = machineMoldMatchingHistoryRepository.findAll();
        machineMatchingHistory.forEach(history -> {
            history.setMatchDay(DateUtils2.format(history.getMatchTime(), DateUtils2.DatePattern.yyyyMMdd, LocationUtils.getZoneIdByLocationId(history.getMachine().getLocationId()), null));
            history.setMatchHour(DateUtils2.format(history.getMatchTime(), "HHmm", LocationUtils.getZoneIdByLocationId(history.getMachine().getLocationId()), null));
            history.setUnmatchDay(DateUtils2.format(history.getUnmatchTime(), DateUtils2.DatePattern.yyyyMMdd, LocationUtils.getZoneIdByLocationId(history.getMachine().getLocationId()), null));
            history.setUnmatchHour(DateUtils2.format(history.getUnmatchTime(), "HHmm", LocationUtils.getZoneIdByLocationId(history.getMachine().getLocationId()), null));
        });
        machineMoldMatchingHistoryRepository.saveAll(machineMatchingHistory);
        return ApiResponse.success("OK", machineMatchingHistory);
    }
}
