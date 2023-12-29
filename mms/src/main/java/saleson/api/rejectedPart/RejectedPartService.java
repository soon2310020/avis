package saleson.api.rejectedPart;

import com.emoldino.framework.util.DateUtils2;
import com.google.common.collect.Maps;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import saleson.api.checklist.ChecklistRepository;
import saleson.api.machine.MachineMoldMatchingHistoryRepository;
import saleson.api.machine.MachineOeeRepository;
import saleson.api.machine.MachineRepository;
import saleson.api.mold.MoldPartRepository;
import saleson.api.mold.MoldRepository;
import saleson.api.rejectedPart.payload.MachineCountEntryRecord;
import saleson.api.rejectedPart.payload.RejectRatePayload;
import saleson.api.rejectedPart.payload.RejectedPartPayload;
import saleson.api.resource.ResourceHandler;
import saleson.api.shiftConfig.DayShiftRepository;
import saleson.api.shiftConfig.ShiftConfigService;
import saleson.api.statistics.StatisticsRepository;
import saleson.api.user.UserRepository;
import saleson.common.constant.CommonMessage;
import saleson.common.enumeration.Comparison;
import saleson.common.enumeration.DayShiftType;
import saleson.common.enumeration.Frequent;
import saleson.common.enumeration.RejectedRateStatus;
import saleson.common.payload.ApiResponse;
import saleson.common.util.DateUtils;
import saleson.common.util.SecurityUtils;
import saleson.common.util.StringUtils;
import saleson.dto.RejectPartEntryRecordDTO;
import saleson.dto.RejectPartEntryRecordItemDTO;
import saleson.dto.RejectRateOEEDTO;
import saleson.dto.common.TwoObject;
import saleson.model.*;
import saleson.model.checklist.CheckListObjectType;
import saleson.model.checklist.Checklist;
import saleson.model.checklist.ChecklistType;
import saleson.model.data.MiniComponentData;
import saleson.model.data.MoldShotData;
import saleson.model.data.rejectedPartRate.RejectedPartBreakDownData;
import saleson.model.rejectedPartRate.ProducedPart;
import saleson.model.rejectedPartRate.RejectedPartDetails;
import saleson.service.util.NumberUtils;

import java.time.Instant;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class RejectedPartService {
    @Autowired
    ProducedPartRepository producedPartRepository;

    @Autowired
    RejectedPartDetailsRepository rejectedPartDetailsRepository;

    @Autowired
    StatisticsRepository statisticsRepository;

    @Autowired
    MoldRepository moldRepository;

    @Autowired
    MoldPartRepository moldPartRepository;

    @Autowired
    private ResourceHandler handler;

    @Autowired
    private ChecklistRepository checklistRepository;

    @Autowired
    private RejectRateConfigurationRepository rejectRateConfigurationRepository;

    @Autowired
    private MachineOeeRepository machineOeeRepository;

    @Autowired
    private MachineMoldMatchingHistoryRepository machineMoldMatchingHistoryRepository;

    @Autowired
    private MachineRepository machineRepository;

    @Autowired
    private DayShiftRepository dayShiftRepository;

    @Autowired
    private ShiftConfigService shiftConfigService;

    @Autowired
    private UserRepository userRepository;

    @Value("${customer.server.name}")
    String serverName;

    public Page<ProducedPart> findAll(RejectedPartPayload payload, Pageable pageable){
        Page<ProducedPart> result = producedPartRepository.findAll(payload.getPredicate(), pageable);
        List<ProducedPart> currentProducedParts = result.getContent();
        if(currentProducedParts != null && currentProducedParts.size() > 0) {
            List<Long> currentMoldIds = currentProducedParts.stream().map(x -> x.getMoldId()).distinct().collect(Collectors.toList());
            processData(currentMoldIds, payload, result);
        }
        return result;
    }

    private void processData(List<Long> currentMoldIds, RejectedPartPayload payload, Page<ProducedPart> result) {
        List<ProducedPart> previousProducedParts;
        Map<Long, List<MachineOee>> moldMachineOeeMap = Maps.newHashMap();
        if (payload.getFrequent() == null || payload.getFrequent().equals(Frequent.DAILY)) {
            String previousDay = DateUtils.getReducedDate(payload.getDay() != null ? payload.getDay() : payload.getStart(), 1L);
            previousProducedParts = producedPartRepository.findByMoldIdInAndFrequentAndDay(currentMoldIds, Frequent.DAILY, previousDay);
            List<MachineOee> machineOeeList = machineOeeRepository.findAllByMoldIdInAndDayAndPeriodType(currentMoldIds, payload.getDay(), Frequent.HOURLY);
            moldMachineOeeMap = machineOeeList.stream().collect(Collectors.groupingBy(MachineOee::getMoldId));
        } else if (payload.getFrequent().equals(Frequent.WEEKLY)) {
            String previousWeek = DateUtils.getPreviousWeek(payload.getStartDate(), payload.getWeek() != null ? payload.getWeek() : payload.getStart());
            previousProducedParts = producedPartRepository.findByMoldIdInAndFrequentAndWeek(currentMoldIds, Frequent.WEEKLY, previousWeek);
        } else if (payload.getFrequent().equals(Frequent.HOURLY)) {
            String previousHour = DateUtils.getPreviousHour(payload.getHour() != null ? payload.getHour() : payload.getStart());
            List<MachineOee> machineOeeList = machineOeeRepository.findAllByMoldIdInAndHourAndPeriodType(currentMoldIds, previousHour, Frequent.HOURLY);
            moldMachineOeeMap = machineOeeList.stream().collect(Collectors.groupingBy(MachineOee::getMoldId));
            previousProducedParts = producedPartRepository.findByMoldIdInAndFrequentAndHour(currentMoldIds, Frequent.HOURLY, previousHour);
        } else {
            String firstDayOfMonth = payload.getMonth() != null ? payload.getMonth() : payload.getStart() + "01";
            String previousMonth = DateUtils.getPreviousMonth(firstDayOfMonth);
            previousProducedParts = producedPartRepository.findByMoldIdInAndFrequentAndMonth(currentMoldIds, Frequent.MONTHLY, previousMonth);
        }
        for (ProducedPart producedPart : result) {
            if(producedPart.getRejectedRateStatus().equals(RejectedRateStatus.UNCOMPLETED)){
                producedPart.setRejectedRate(null);
                producedPart.setTotalRejectedAmount(null);
            }
            if (payload.getFrequent() == null || payload.getFrequent()== Frequent.DAILY ||  payload.getFrequent()== Frequent.HOURLY) {
                List<MachineOee> machineOeeList = moldMachineOeeMap.get(producedPart.getMoldId());
                if (CollectionUtils.isNotEmpty(machineOeeList)) {
                    producedPart.setMachineList(machineOeeList.stream().map(MachineOee::getMachine).distinct().collect(Collectors.toList()));
                }
            }

            ProducedPart prevProducedPart = previousProducedParts.stream().filter(x -> x.getMoldId().equals(producedPart.getMoldId()) && x.getPartId().equals(producedPart.getPartId())).findAny().orElse(null);
            if(prevProducedPart != null && prevProducedPart.getRejectedRate() != null && producedPart.getRejectedRate() != null){
                if(producedPart.getRejectedRate() >= prevProducedPart.getRejectedRate())
                    producedPart.setComparison(Comparison.UP);
                else
                    producedPart.setComparison(Comparison.DOWN);
            }
        }

    }

    public void test(Long statisticsId, Long moldId){
        Statistics statistics = statisticsRepository.findById(statisticsId).orElse(null);
        List<MoldPart> moldParts = moldPartRepository.findAllByMoldIdOrderById(moldId);
        saveProducedPart(statistics, moldParts);

    }

    // save produced part updated at any time receiving new data, for Rejected Part log
    public void saveProducedPart(Statistics statistics, List<MoldPart> moldParts){
        try {
            if(statistics.getShotCount() <= 0) return;
            moldParts.forEach(moldPart -> {
                generateHourlyData(statistics, moldPart);
                generateDailyData(statistics, moldPart);
                generateWeeklyData(statistics, moldPart);
                generateMonthlyData(statistics, moldPart);
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void generateDailyData(Statistics statistics, MoldPart moldPart) {
        List<ProducedPart> producedPartList = producedPartRepository.findByMoldIdAndPartIdAndDayAndFrequent(moldPart.getMoldId(), moldPart.getPartId(), statistics.getDay(), Frequent.DAILY);
        ProducedPart producedPart = ProducedPart.builder()
                .mold(moldPart.getMold())
                .part(moldPart.getPart())
                .hour(statistics.getHour())
                .day(statistics.getDay())
                .week(statistics.getWeek())
                .month(statistics.getMonth())
                .frequent(Frequent.DAILY)
                .totalProducedAmount(0)
                .totalRejectedAmount(0)
                .rejectedRate(0.0)
                .avgRejectedRate(0.0)
                .rejectedRateStatus(RejectedRateStatus.UNCOMPLETED)
                .rejectedPartDetails(new HashSet<>())
                .build();
        if(producedPartList != null && producedPartList.size() > 0) producedPart = producedPartList.get(0);
        if (statistics.getCt() > 0 || statistics.getFirstData()) {
            int currentProduced = producedPart.getTotalProducedAmount() != null ? producedPart.getTotalProducedAmount() : 0;
            producedPart.setTotalProducedAmount(currentProduced + statistics.getShotCount() * moldPart.getCavity());
        }

        if(producedPart.getId() == null){
            ProducedPart lastCompleted = producedPartRepository.findFirstByMoldIdAndPartIdAndRejectedRateStatusAndFrequentOrderByDayDesc(moldPart.getMoldId(), moldPart.getPartId(), RejectedRateStatus.COMPLETED, Frequent.DAILY).orElse(null);
            if(lastCompleted != null) producedPart.setAvgRejectedRate(lastCompleted.getAvgRejectedRate());
        }else if(producedPart.getRejectedRateStatus() == RejectedRateStatus.COMPLETED){
            producedPart.setRejectedRate(NumberUtils.roundOffNumber(producedPart.getTotalRejectedAmount() * 100.0 / producedPart.getTotalProducedAmount()));

            // Current statistics is the closest one => the previous data is the second one
            List<ProducedPart> lastCompleted = producedPartRepository.findTop2ByMoldIdAndPartIdAndRejectedRateStatusAndFrequentOrderByDayDesc(moldPart.getMoldId(), moldPart.getPartId(), RejectedRateStatus.COMPLETED, Frequent.DAILY);
            Long countCompleted = producedPartRepository.countByMoldIdAndPartIdAndRejectedRateStatusAndFrequent(moldPart.getMoldId(), moldPart.getPartId(), RejectedRateStatus.COMPLETED, Frequent.DAILY);

            Double oldAvgRate = 0.0;
            if(lastCompleted != null && lastCompleted.size() == 2)
                oldAvgRate = lastCompleted.get(1).getAvgRejectedRate();

            Double avgRate = (oldAvgRate * (countCompleted - 1) + producedPart.getRejectedRate()) / countCompleted;
            producedPart.setAvgRejectedRate(avgRate);
        }

        producedPartRepository.save(producedPart);
    }

    private void generateHourlyData(Statistics statistics, MoldPart moldPart) {
        List<ProducedPart> producedPartList = producedPartRepository.findByMoldIdAndPartIdAndHourAndFrequent(moldPart.getMoldId(), moldPart.getPartId(), statistics.getHour(), Frequent.HOURLY);
        ProducedPart producedPart = ProducedPart.builder()
                .mold(moldPart.getMold())
                .part(moldPart.getPart())
                .hour(statistics.getHour())
                .day(statistics.getDay())
                .week(statistics.getWeek())
                .month(statistics.getMonth())
                .frequent(Frequent.HOURLY)
                .totalProducedAmount(0)
                .totalRejectedAmount(0)
                .rejectedRate(0.0)
                .avgRejectedRate(0.0)
                .rejectedRateStatus(RejectedRateStatus.UNCOMPLETED)
                .rejectedPartDetails(new HashSet<>())
                .build();
        if(producedPartList != null && producedPartList.size() > 0) producedPart = producedPartList.get(0);
        if (statistics.getCt() > 0 || statistics.getFirstData()) {
            int currentProduced = producedPart.getTotalProducedAmount() != null ? producedPart.getTotalProducedAmount() : 0;
            producedPart.setTotalProducedAmount(currentProduced + statistics.getShotCount() * moldPart.getCavity());
        }

        if(producedPart.getId() == null){
            ProducedPart lastCompleted = producedPartRepository.findFirstByMoldIdAndPartIdAndRejectedRateStatusAndFrequentOrderByHourDesc(moldPart.getMoldId(), moldPart.getPartId(), RejectedRateStatus.COMPLETED, Frequent.HOURLY).orElse(null);
            if(lastCompleted != null) producedPart.setAvgRejectedRate(lastCompleted.getAvgRejectedRate());
        }else if(producedPart.getRejectedRateStatus() == RejectedRateStatus.COMPLETED){
            producedPart.setRejectedRate(NumberUtils.roundOffNumber(producedPart.getTotalRejectedAmount() * 100.0 / producedPart.getTotalProducedAmount()));

            // Current statistics is the closest one => the previous data is the second one
            List<ProducedPart> lastCompleted = producedPartRepository.findTop2ByMoldIdAndPartIdAndRejectedRateStatusAndFrequentOrderByHourDesc(moldPart.getMoldId(), moldPart.getPartId(), RejectedRateStatus.COMPLETED, Frequent.HOURLY);
            Long countCompleted = producedPartRepository.countByMoldIdAndPartIdAndRejectedRateStatusAndFrequent(moldPart.getMoldId(), moldPart.getPartId(), RejectedRateStatus.COMPLETED, Frequent.HOURLY);

            Double oldAvgRate = 0.0;
            if(lastCompleted != null && lastCompleted.size() == 2)
                oldAvgRate = lastCompleted.get(1).getAvgRejectedRate();

            Double avgRate = (oldAvgRate * (countCompleted - 1) + producedPart.getRejectedRate()) / countCompleted;
            producedPart.setAvgRejectedRate(avgRate);
        }

        producedPartRepository.save(producedPart);
    }

    private void generateWeeklyData(Statistics statistics, MoldPart moldPart) {
        List<ProducedPart> producedPartList = producedPartRepository.findByMoldIdAndPartIdAndWeekAndFrequent(moldPart.getMoldId(), moldPart.getPartId(), statistics.getWeek(), Frequent.WEEKLY);
        ProducedPart producedPart = ProducedPart.builder()
                .mold(moldPart.getMold())
                .part(moldPart.getPart())
                .hour(statistics.getHour())
                .day(statistics.getDay())
                .week(statistics.getWeek())
                .month(statistics.getMonth())
                .frequent(Frequent.WEEKLY)
                .totalProducedAmount(0)
                .totalRejectedAmount(0)
                .rejectedRate(0.0)
                .avgRejectedRate(0.0)
                .rejectedRateStatus(RejectedRateStatus.UNCOMPLETED)
                .rejectedPartDetails(new HashSet<>())
                .build();
        if(producedPartList != null && producedPartList.size() > 0) producedPart = producedPartList.get(0);
        if (statistics.getCt() > 0 || statistics.getFirstData()) {
            int currentProduced = producedPart.getTotalProducedAmount() != null ? producedPart.getTotalProducedAmount() : 0;
            producedPart.setTotalProducedAmount(currentProduced + statistics.getShotCount() * moldPart.getCavity());
        }

        if(producedPart.getId() == null){
            ProducedPart lastCompleted = producedPartRepository.findFirstByMoldIdAndPartIdAndRejectedRateStatusAndFrequentOrderByWeekDesc(moldPart.getMoldId(), moldPart.getPartId(), RejectedRateStatus.COMPLETED, Frequent.WEEKLY).orElse(null);
            if(lastCompleted != null) producedPart.setAvgRejectedRate(lastCompleted.getAvgRejectedRate());
        }else if(producedPart.getRejectedRateStatus() == RejectedRateStatus.COMPLETED){
            producedPart.setRejectedRate(NumberUtils.roundOffNumber(producedPart.getTotalRejectedAmount() * 100.0 / producedPart.getTotalProducedAmount()));

            // Current statistics is the closest one => the previous data is the second one
            List<ProducedPart> lastCompleted = producedPartRepository.findTop2ByMoldIdAndPartIdAndRejectedRateStatusAndFrequentOrderByWeekDesc(moldPart.getMoldId(), moldPart.getPartId(), RejectedRateStatus.COMPLETED, Frequent.WEEKLY);
            Long countCompleted = producedPartRepository.countByMoldIdAndPartIdAndRejectedRateStatusAndFrequent(moldPart.getMoldId(), moldPart.getPartId(), RejectedRateStatus.COMPLETED, Frequent.WEEKLY);

            Double oldAvgRate = 0.0;
            if(lastCompleted != null && lastCompleted.size() == 2)
                oldAvgRate = lastCompleted.get(1).getAvgRejectedRate();

            Double avgRate = (oldAvgRate * (countCompleted - 1) + producedPart.getRejectedRate()) / countCompleted;
            producedPart.setAvgRejectedRate(avgRate);
        }

        producedPartRepository.save(producedPart);
    }

    private void generateMonthlyData(Statistics statistics, MoldPart moldPart) {
        List<ProducedPart> producedPartList = producedPartRepository.findByMoldIdAndPartIdAndMonthAndFrequent(moldPart.getMoldId(), moldPart.getPartId(), statistics.getMonth(), Frequent.MONTHLY);
        ProducedPart producedPart = ProducedPart.builder()
                .mold(moldPart.getMold())
                .part(moldPart.getPart())
                .hour(statistics.getHour())
                .day(statistics.getDay())
                .week(statistics.getWeek())
                .month(statistics.getMonth())
                .frequent(Frequent.MONTHLY)
                .totalProducedAmount(0)
                .totalRejectedAmount(0)
                .rejectedRate(0.0)
                .avgRejectedRate(0.0)
                .rejectedRateStatus(RejectedRateStatus.UNCOMPLETED)
                .rejectedPartDetails(new HashSet<>())
                .build();
        if(producedPartList != null && producedPartList.size() > 0) producedPart = producedPartList.get(0);
        if (statistics.getCt() > 0 || statistics.getFirstData()) {
            int currentProduced = producedPart.getTotalProducedAmount() != null ? producedPart.getTotalProducedAmount() : 0;
            producedPart.setTotalProducedAmount(currentProduced + statistics.getShotCount() * moldPart.getCavity());
        }

        if(producedPart.getId() == null){
            ProducedPart lastCompleted = producedPartRepository.findFirstByMoldIdAndPartIdAndRejectedRateStatusAndFrequentOrderByMonthDesc(moldPart.getMoldId(), moldPart.getPartId(), RejectedRateStatus.COMPLETED, Frequent.MONTHLY).orElse(null);
            if(lastCompleted != null) producedPart.setAvgRejectedRate(lastCompleted.getAvgRejectedRate());
        }else if(producedPart.getRejectedRateStatus() == RejectedRateStatus.COMPLETED){
            producedPart.setRejectedRate(NumberUtils.roundOffNumber(producedPart.getTotalRejectedAmount() * 100.0 / producedPart.getTotalProducedAmount()));

            // Current statistics is the closest one => the previous data is the second one
            List<ProducedPart> lastCompleted = producedPartRepository.findTop2ByMoldIdAndPartIdAndRejectedRateStatusAndFrequentOrderByMonthDesc(moldPart.getMoldId(), moldPart.getPartId(), RejectedRateStatus.COMPLETED, Frequent.MONTHLY);
            Long countCompleted = producedPartRepository.countByMoldIdAndPartIdAndRejectedRateStatusAndFrequent(moldPart.getMoldId(), moldPart.getPartId(), RejectedRateStatus.COMPLETED, Frequent.MONTHLY);

            Double oldAvgRate = 0.0;
            if(lastCompleted != null && lastCompleted.size() == 2)
                oldAvgRate = lastCompleted.get(1).getAvgRejectedRate();

            Double avgRate = (oldAvgRate * (countCompleted - 1) + producedPart.getRejectedRate()) / countCompleted;
            producedPart.setAvgRejectedRate(avgRate);
        }

        producedPartRepository.save(producedPart);
    }

    @Transactional
    public List<ProducedPart> setRejectedParts(List<RejectedPartPayload> payloads){
        List<ProducedPart> updatedProducedParts = new ArrayList<>();
        User user = userRepository.getOne(SecurityUtils.getUserId());
        payloads.forEach(payload -> {
            if(payload.getId() == null) return;
            ProducedPart producedPart = producedPartRepository.findById(payload.getId()).orElse(null);
            if(producedPart == null) return;
            if(producedPart.getRejectedPartDetails() != null) {
                rejectedPartDetailsRepository.deleteAll(producedPart.getRejectedPartDetails());
            }
            Set<RejectedPartDetails> rejectedPartDetails = payload.getRejectedParts();
            rejectedPartDetails.forEach(rejectedPart -> rejectedPart.setProducedPart(producedPart));
            rejectedPartDetailsRepository.saveAll(rejectedPartDetails);

            Integer totalRejectedAmount = rejectedPartDetails.stream().map(x -> x.getRejectedAmount()).reduce(0, Integer::sum);
            producedPart.setRejectedPartDetails(new HashSet<>(rejectedPartDetails));
            producedPart.setTotalRejectedAmount(totalRejectedAmount);
            producedPart.setEditedBy(SecurityUtils.getName());
            producedPart.setReportedBy(user);
            producedPart.setReportedById(SecurityUtils.getUserId());
            producedPart.setRejectedRateStatus(RejectedRateStatus.COMPLETED);
            Double rawRejectRate = producedPart.getTotalProducedAmount() == 0 ? 0 : producedPart.getTotalRejectedAmount() * 100.0 / producedPart.getTotalProducedAmount();
            producedPart.setRejectedRate(NumberUtils.roundOffNumber(rawRejectRate));

            Long countOldProducedParts = producedPartRepository.countByMoldIdAndPartIdAndRejectedRateStatusAndFrequent(producedPart.getMoldId(), producedPart.getPartId(), RejectedRateStatus.COMPLETED, producedPart.getFrequent());
            Double avgRejectedRate = (producedPart.getAvgRejectedRate() * countOldProducedParts + producedPart.getRejectedRate()) / (countOldProducedParts + 1);
            producedPart.setAvgRejectedRate(avgRejectedRate);
            updatedProducedParts.add(producedPart);

            //update daily data when update hourly
            if (Frequent.HOURLY.equals(producedPart.getFrequent())) {
                List<ProducedPart> list = producedPartRepository.findByMoldIdAndPartIdAndDayAndFrequent(producedPart.getMoldId(), producedPart.getPartId(), producedPart.getDay(), Frequent.DAILY);
                if (CollectionUtils.isNotEmpty(list)) {
                    ProducedPart daily = list.get(0);
                    daily.setTotalRejectedAmount(totalRejectedAmount);
                    daily.setEditedBy(SecurityUtils.getName());
                    daily.setReportedBy(user);
                    daily.setReportedById(SecurityUtils.getUserId());
                    daily.setRejectedRateStatus(RejectedRateStatus.COMPLETED);
                    Long countOldProducedPartsDaily = producedPartRepository.countByMoldIdAndPartIdAndRejectedRateStatusAndFrequent(daily.getMoldId(), daily.getPartId(), RejectedRateStatus.COMPLETED, daily.getFrequent());
                    Double dailyAvgRejectedRate = daily.getAvgRejectedRate() == null ? 0 : daily.getAvgRejectedRate();
                    Double dailyRejectedRate = daily.getRejectedRate() == null ? 0 : daily.getRejectedRate();
                    Double avgRejectedRateDaily = (dailyAvgRejectedRate * countOldProducedPartsDaily + dailyRejectedRate) / (countOldProducedPartsDaily + 1);
                    daily.setAvgRejectedRate(avgRejectedRateDaily);
                    updatedProducedParts.add(daily);
                }
            }
            //update oee data
            if (Frequent.HOURLY.equals(producedPart.getFrequent()) && producedPart.getMold() != null) {
                Optional<List<MachineOee>> optional = machineOeeRepository.findByMoldAndHourAndPeriodType(producedPart.getMold(), producedPart.getHour(), Frequent.HOURLY);
                if (optional.isPresent()) {
                    MachineOee machineOee = optional.get().get(0);
                    machineOee.setFq((100 - rawRejectRate));
                    machineOee.setRejectedPart(producedPart.getTotalRejectedAmount().longValue());
                    machineOeeRepository.save(machineOee);
                }
            }
        });
        return producedPartRepository.saveAll(updatedProducedParts);
    }

    public List<RejectedPartBreakDownData> getBreakdownData(RejectedPartPayload payload){
        if(payload.getMoldId() == null || payload.getPartId() == null)
            return null;
                List<RejectedPartBreakDownData> result;
            if(!StringUtils.isEmpty(payload.getStart()) && !StringUtils.isEmpty(payload.getEnd())) {
                result = rejectedPartDetailsRepository.findBreakDownByMoldIdAndPartIdAndFrequentAndPeriod(
                        payload.getMoldId(), payload.getPartId(), payload.getFrequent(), payload.getStart(), payload.getEnd());
            } else {
                String selectedTime = payload.getFrequent().equals(Frequent.WEEKLY) ? payload.getWeek()
                        : (payload.getFrequent().equals(Frequent.MONTHLY) ? payload.getMonth()
                        : (payload.getFrequent().equals(Frequent.HOURLY) ? payload.getHour()
                        : payload.getDay()));
                result = rejectedPartDetailsRepository.findBreakDownByMoldIdAndPartIdAndFrequent(
                        payload.getMoldId(), payload.getPartId(), payload.getFrequent(), selectedTime);
            }
        Integer totalRejectedAmount = result.stream().map(x -> x.getRejectedAmount()).reduce(0, Integer::sum);
        result.forEach(data -> {
            if(totalRejectedAmount != 0) {
                Double rejectedRate = data.getRejectedAmount() * 100.0 / totalRejectedAmount;
                data.setRejectedRate(NumberUtils.roundOffNumber(rejectedRate));
            }else{
                data.setRejectedRate(0.0);
            }
        });
        return result;
    }

    public ApiResponse getRejectRateReason(Long id) {
        Map<String,String> resources= handler.getAllMessagesOfCurrentUser();
        List<String> defaultReasons = Arrays.asList(
                resources.get("black_dot"),
                resources.get("bubbles"),
                resources.get("burn_mark"),
                resources.get("dented"),
                resources.get("drag_mark"),
                resources.get("flashing"),
                resources.get("flow_mark"),
                resources.get("gas_mark"),
                resources.get("loose_burr"),
                resources.get("missing"),
                resources.get("oily"),
                resources.get("parting_line"),
                resources.get("pin_mark"),
                resources.get("wrinkle"));
        ProducedPart producedPart = producedPartRepository.getOne(id);
        if (CollectionUtils.isNotEmpty(producedPart.getRejectedPartDetails())) {
            List<String> actualReasons = producedPart.getRejectedPartDetails().stream().map(RejectedPartDetails::getReason).collect(Collectors.toList());
            return ApiResponse.success(CommonMessage.OK, actualReasons);
        } else {
            Mold mold = moldRepository.getOne(producedPart.getMoldId());
            Checklist checklist = checklistRepository.findFirstByCompanyIdAndChecklistTypeAndObjectTypeAndEnabledIsTrue(mold.getLocation().getCompanyId(), ChecklistType.REJECT_RATE, CheckListObjectType.CHECK_LIST).orElse(null);
            if (checklist == null) {
                return ApiResponse.success(CommonMessage.OK, defaultReasons);
            } else {
                return ApiResponse.success(CommonMessage.OK, checklist.getChecklistItems());
            }
        }

    }

    @Transactional
    public Page<RejectRateOEEDTO> getRejectRateOee(RejectRatePayload payload, Pageable pageable) {
//        if ("mm0427".equals(serverName)) {
//            return new PageImpl<>(new ArrayList<>(), pageable, 0);
//        }
        DayShift dayShift = getDayShiftByLocationIdAndDay(payload.getLocationIdList().get(0), payload.getDay());
        TwoObject<String, String> dayShiftHour = getDayShiftHour(payload.getDay(), dayShift);
//        List<Long> machineIdList = producedPartRepository.findAllMachineIdRejectRateOEE(payload, dayShiftHour.getLeft(), dayShiftHour.getRight());
//        payload.setMachineIdList(machineIdList);
        List<RejectRateOEEDTO> rejectRateOEEList = producedPartRepository.findRejectRateOEE(payload, pageable, dayShiftHour.getLeft(), dayShiftHour.getRight());
        long total = producedPartRepository.countRejectRateOEE(payload, pageable, dayShiftHour.getLeft(), dayShiftHour.getRight());
        setMoldToRejectRateOee(rejectRateOEEList, payload, dayShiftHour);
        return new PageImpl<>(rejectRateOEEList, pageable, total);
    }

    private void setMoldToRejectRateOee(List<RejectRateOEEDTO> rejectRateOEEList, RejectRatePayload payload, TwoObject<String, String> dayShiftHour) {
        List<Long> machineIdList = rejectRateOEEList.stream().map(RejectRateOEEDTO::getMachineId).collect(Collectors.toList());

        List<MachineOee> machineOeeAllList =  machineOeeRepository.findAllByMachineIdInAndHourBetweenAndPeriodType(machineIdList, dayShiftHour.getLeft(), dayShiftHour.getRight(), Frequent.HOURLY);
        List<MachineCountEntryRecord> machineCountEntryRecordList = producedPartRepository.countMachineRejectRateOEE(machineIdList, payload.getDay(), dayShiftHour.getLeft(), dayShiftHour.getRight(), null);

        Map<Long, MachineCountEntryRecord> machineCountEntryRecordMap = machineCountEntryRecordList.stream().collect(Collectors.toMap(MachineCountEntryRecord::getMachineId, Function.identity(), (o,n)->n));
        Map<Long, List<MachineOee>> machineOEEMap = machineOeeAllList.stream().collect(Collectors.groupingBy(MachineOee::getMachineId));


        rejectRateOEEList.forEach(rejectRateOEEDTO -> {
            List<MachineOee> machineOeeList = machineOEEMap.get(rejectRateOEEDTO.getMachineId());
            if (CollectionUtils.isNotEmpty(machineOeeList)) {
                rejectRateOEEDTO.setMoldList(machineOeeList.stream().map(MachineOee::getMold).filter(Objects::nonNull).map(MiniComponentData::new).distinct().collect(Collectors.toList()));
            }

            MachineCountEntryRecord machineCountEntryRecord = machineCountEntryRecordMap.get(rejectRateOEEDTO.getMachineId());
            if (machineCountEntryRecord != null) {
                rejectRateOEEDTO.setEntryRecord(machineCountEntryRecord.getCount());
            }
        });
    }

    private  TwoObject<String, String> getDayShiftHour(String day, DayShift dayShift) {
        String startHour = day.concat(dayShift.getStart().substring(0, 2));
        if (Integer.parseInt(dayShift.getStart()) >= Integer.parseInt(dayShift.getEnd())) {
            day = DateUtils.getNextDay(DateUtils.getInstant(day, DateUtils.YYYY_MM_DD_DATE_FORMAT), DateUtils.YYYY_MM_DD_DATE_FORMAT, 1);
        }
        String endHour =  day.concat(dayShift.getEnd().substring(0, 2));
        return new TwoObject<>(startHour, String.valueOf(Long.parseLong(endHour) - 1));
    }

    public RejectPartEntryRecordDTO getRejectPartEntryRecord(Long machineId, String day, Pageable pageable) {
        RejectPartEntryRecordDTO response = new RejectPartEntryRecordDTO();
        Optional<Machine> machineOptional = machineRepository.findById(machineId);
        if (machineOptional.isPresent()) {
            Machine machine = machineOptional.get();
            DayShift dayShift = getDayShiftByLocationIdAndDay(machine.getLocationId(), day);
            TwoObject<String, String> dayShiftHour = getDayShiftHour(day, dayShift);
            response.setMachineId(machineId);
            response.setMachineCode(machine.getMachineCode());
            response.setDate(day);
            response.setStart(dayShift.getStart());
            response.setEnd(dayShift.getEnd());
            response.setNumberOfShift(dayShift.getNumberOfShifts());

            List<HourShift> hourShiftList = dayShift.getHourShifts();
            List<RejectPartEntryRecordItemDTO> rejectPartEntryRecordItemList = producedPartRepository.findRejectPartEntryRecord(machineId, day, pageable, dayShiftHour.getLeft(), dayShiftHour.getRight());
            Map<String,List<MachineOee>> machineOeeListMap = machineOeeRepository.findAllByMachineIdAndHourBetweenAndPeriodType(machineId, dayShiftHour.getLeft(), dayShiftHour.getRight(), Frequent.HOURLY)
                    .stream().collect(Collectors.groupingBy(MachineOee::getHour));
            rejectPartEntryRecordItemList.forEach(rejectPartEntryRecordItemDTO -> {
                HourShift hourShift = getHourShiftByDay(rejectPartEntryRecordItemDTO.getHour(), hourShiftList);
                rejectPartEntryRecordItemDTO.setStart(hourShift.getStart());
                rejectPartEntryRecordItemDTO.setEnd(hourShift.getEnd());
                rejectPartEntryRecordItemDTO.setShiftNumber(hourShift.getShiftNumber());
                List<MachineOee> machineOeeList = machineOeeListMap.get(rejectPartEntryRecordItemDTO.getHour());
                if (CollectionUtils.isNotEmpty(machineOeeList)) {
                    rejectPartEntryRecordItemDTO.setMoldList(machineOeeList.stream().map(MachineOee::getMold).map(MiniComponentData::new).collect(Collectors.toList()));
                }
            });
            long total = producedPartRepository.countEntryRecord(machineId, day, dayShift.getStart(), dayShift.getEnd(), pageable);
            Page<RejectPartEntryRecordItemDTO> rejectPartEntryRecordPage = new PageImpl<>(rejectPartEntryRecordItemList, pageable, total);
            response.setRejectPartEntryRecordPage(rejectPartEntryRecordPage);
        }
        return response;
    }

    private HourShift getHourShiftByDay(String hour, List<HourShift> hourShiftList) {
        int hourNumber = Integer.parseInt(hour.substring(hour.length() - 2));

        for (HourShift hourShift : hourShiftList) {
            int start = Integer.parseInt(hourShift.getStart().substring(0, 2));
            int end = Integer.parseInt(hourShift.getEnd().substring(0, 2));
            if(start <= hourNumber && hourNumber < end) {
                return hourShift;
            }
        }

        return new HourShift();
    }

    public ApiResponse fixProducedPartData() {
        try {
            List<ProducedPart> producedParts = producedPartRepository.findAll();

            producedParts.forEach(producedPart -> {
                MoldShotData data;
                if (Frequent.MONTHLY.equals(producedPart.getFrequent())) {
                  data = moldRepository.findProducedPartData(producedPart.getMoldId(), producedPart.getMonth(), Frequent.MONTHLY);
                } else if (Frequent.WEEKLY.equals(producedPart.getFrequent())) {
                    data = moldRepository.findProducedPartData(producedPart.getMoldId(), producedPart.getWeek(), Frequent.WEEKLY);
                } else {
                    data = moldRepository.findProducedPartData(producedPart.getMoldId(), producedPart.getDay(), Frequent.DAILY);
                }

                producedPart.setTotalProducedAmount(data.getQuantity());
            });
            return ApiResponse.success(CommonMessage.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return ApiResponse.error();
        }
    }

    public ApiResponse setConfiguration(String frequent) {
        try {
            Frequent newFrequent = Frequent.valueOf(frequent);
            RejectRateConfiguration configuration = rejectRateConfigurationRepository.getOne(1L);
            configuration.setFrequent(newFrequent);
            rejectRateConfigurationRepository.save(configuration);

            //remove other frequent data and update related data
            removeOtherFrequentData(newFrequent);

            return ApiResponse.success(CommonMessage.OK, configuration);
        } catch (Exception e) {
            e.printStackTrace();
            return ApiResponse.error();
        }
    }

    private void removeOtherFrequentData(Frequent frequent){
//        String currentHour = DateUtils.getHour(DateUtils.getDate(Instant.now(), DateUtils.DEFAULT_DATE_FORMAT));
        String currentDay = DateUtils.getToday(DateUtils.YYYY_MM_DD_DATE_FORMAT);
        String currentWeek = DateUtils.getYearWeek(Instant.now(), DateUtils.DEFAULT_DATE_FORMAT);
        String currentMonth = DateUtils.getYearMonth(Instant.now(), DateUtils.DEFAULT_DATE_FORMAT);

        switch (frequent){
            case HOURLY:
                removeRejectRateData(producedPartRepository.findByDayAndFrequent(currentDay, Frequent.DAILY));
                removeRejectRateData(producedPartRepository.findByWeekAndFrequent(currentWeek, Frequent.WEEKLY));
                removeRejectRateData(producedPartRepository.findByMonthAndFrequent(currentMonth, Frequent.MONTHLY));
            case DAILY:
                removeRejectRateData(producedPartRepository.findByDayAndFrequent(currentDay, Frequent.HOURLY));
                updateOEEData(producedPartRepository.findByDayAndFrequent(currentDay, Frequent.HOURLY));
                removeRejectRateData(producedPartRepository.findByWeekAndFrequent(currentWeek, Frequent.WEEKLY));
                removeRejectRateData(producedPartRepository.findByMonthAndFrequent(currentMonth, Frequent.MONTHLY));
            case WEEKLY:
                removeRejectRateData(producedPartRepository.findByWeekAndFrequent(currentDay, Frequent.HOURLY));
                updateOEEData(producedPartRepository.findByWeekAndFrequent(currentDay, Frequent.HOURLY));
                removeRejectRateData(producedPartRepository.findByWeekAndFrequent(currentWeek, Frequent.DAILY));
                removeRejectRateData(producedPartRepository.findByMonthAndFrequent(currentMonth, Frequent.MONTHLY));
            case MONTHLY:
                removeRejectRateData(producedPartRepository.findByMonthAndFrequent(currentMonth, Frequent.HOURLY));
                updateOEEData(producedPartRepository.findByMonthAndFrequent(currentMonth, Frequent.HOURLY));
                removeRejectRateData(producedPartRepository.findByMonthAndFrequent(currentMonth, Frequent.DAILY));
                removeRejectRateData(producedPartRepository.findByMonthAndFrequent(currentMonth, Frequent.WEEKLY));
            default:
        }

    }

    private void removeRejectRateData(List<ProducedPart> producedParts) {
        producedParts.forEach(p -> {
            p.setRejectedRate(0D);
            p.setAvgRejectedRate(0D);
            p.setTotalRejectedAmount(0);
            p.setRejectedRateStatus(RejectedRateStatus.UNCOMPLETED);

            Set<RejectedPartDetails> rejectedPartDetails = p.getRejectedPartDetails();
            rejectedPartDetailsRepository.deleteAll(rejectedPartDetails);
        });
    }

    private void updateOEEData(List<ProducedPart> producedParts) {
        producedParts.forEach(p -> {
            if (p.getMold() != null && p.getMold().getMachine() != null) {
                Machine machine = p.getMold().getMachine();
                Optional<List<MachineOee>> optional = machineOeeRepository.findByMachineAndHourAndPeriodType(machine, p.getHour(), Frequent.HOURLY);
                if (optional.isPresent()) {
                    MachineOee machineOee = optional.get().get(0);
                    machineOee.setFq(100D);
                    machineOeeRepository.save(machineOee);
                }
            }
        });
    }

    public ApiResponse getConfiguration() {
        try {
            RejectRateConfiguration configuration = rejectRateConfigurationRepository.getOne(1L);
            return ApiResponse.success(CommonMessage.OK, configuration);
        } catch (Exception e) {
            e.printStackTrace();
            return ApiResponse.error();
        }
    }

    public ApiResponse getByHourAndMachine_Old(Long machineId, String hour) {
        try {
            Instant time = DateUtils2.toInstant(hour, DateUtils2.DatePattern.yyyyMMddHH, DateUtils2.Zone.SYS);
            Instant time2 = DateUtils.getInstant(hour, DateUtils2.DatePattern.yyyyMMddHH);
            System.out.println(time);
            System.out.println(time2);
            Long moldId = moldRepository.findMoldIdByMachineAndTime(machineId, time);
            if (moldId != null) {
                ProducedPart producedPart = producedPartRepository.findFirstByMoldIdAndFrequentAndHour(moldId, Frequent.HOURLY, hour);
                return ApiResponse.success(CommonMessage.OK, producedPart);
            }
            return ApiResponse.error(CommonMessage.OBJECT_NOT_FOUND);
        } catch (Exception e) {
            e.printStackTrace();
            return ApiResponse.error();
        }
    }

    public ApiResponse getByHourAndMachine(Long machineId, String hour) {
        try {
            Machine machine = machineRepository.getOne(machineId);
            Optional<List<MachineOee>> optional = machineOeeRepository.findByMachineAndHourAndPeriodType(machine, hour, Frequent.HOURLY);

            if (optional.isPresent() && optional.get().get(0).getMoldId() != null) {
                ProducedPart producedPart = producedPartRepository.findFirstByMoldIdAndFrequentAndHour(optional.get().get(0).getMoldId(), Frequent.HOURLY, hour);
                return ApiResponse.success(CommonMessage.OK, producedPart);
            }
            return ApiResponse.error(CommonMessage.OBJECT_NOT_FOUND);
        } catch (Exception e) {
            e.printStackTrace();
            return ApiResponse.error();
        }
    }

    public Page<RejectedPartDetails> getRejectPartSummary(Long machineId, String day, Pageable pageable) {
        Machine machine = machineRepository.getOne(machineId);
        DayShift dayShift = getDayShiftByLocationIdAndDay(machine.getLocationId(), day);
        TwoObject<String, String> dayShiftHour = getDayShiftHour(day, dayShift);
        List<RejectedPartDetails> rejectedPartDetailsList = rejectedPartDetailsRepository.getRejectedPartDetailsByMachineIdAndDay(machineId, day, pageable, dayShiftHour.getLeft(), dayShiftHour.getRight());
        long total = rejectedPartDetailsRepository.countRejectedPartDetailsByMachineIdAndDay(machineId, day,  dayShiftHour.getLeft(), dayShiftHour.getRight());
        return new PageImpl<>(rejectedPartDetailsList, pageable, total);
    }

    private DayShift getDayShiftByLocationIdAndDay(Long locationId, String day) {
        DayShiftType dayShiftType = shiftConfigService.getDayShiftType(day, locationId);
        List<DayShift> dayShifts = dayShiftRepository.findByLocationIdAndDayShiftType(locationId, dayShiftType);
        DayShift dayShift;
        if (CollectionUtils.isNotEmpty(dayShifts)) {
            dayShift = dayShifts.get(0);
        } else {
            dayShift = shiftConfigService.generateDefaultDayShift(locationId, dayShiftType);
        }
        return dayShift;
    }

}
