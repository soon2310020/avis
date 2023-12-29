package saleson.api.machineDowntimeAlert;

import com.emoldino.api.common.resource.base.location.util.LocationUtils;
import com.emoldino.framework.util.BeanUtils;
import com.emoldino.framework.util.DateUtils2;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;
import saleson.api.machine.MachineOeeRepository;
import saleson.api.machineDowntimeAlert.payload.MachineDowntimeAlertData;
import saleson.api.machineDowntimeAlert.payload.SearchMachineDowntimePayload;
import saleson.api.shiftConfig.ShiftConfigService;
import saleson.common.constant.CommonMessage;
import saleson.common.enumeration.Frequent;
import saleson.common.enumeration.MachineDowntimeAlertStatus;
import saleson.common.payload.ApiResponse;
import saleson.common.util.DateUtils;
import saleson.common.util.SecurityUtils;
import saleson.common.util.StringUtils;
import saleson.dto.common.ThreeObject;
import saleson.model.MachineDowntimeAlert;
import saleson.model.MachineDowntimeReason;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import saleson.api.machineDowntimeAlert.data.MachineDowntimeReasonData;
import saleson.model.MachineOee;
import saleson.model.Mold;

import java.util.ArrayList;

@Slf4j
@Service
public class MachineDowntimeAlertService {
    @Autowired
    private MachineDowntimeAlertRepository machineDowntimeAlertRepository;

    @Autowired
    private MachineDowntimeReasonRepository machineDowntimeReasonRepository;

    @Autowired
    private MachineOeeRepository machineOeeRepository;

    public Page<MachineDowntimeAlertData> getMachineDowntimeAlert(SearchMachineDowntimePayload payload,
                                                                  Pageable pageable) {
        if(payload.getLocationIdList() != null && payload.getLocationIdList().size() > 0 && payload.getFromDate() != null && payload.getToDate() != null) {
            Pair<String, String> startEndByShift = BeanUtils.get(ShiftConfigService.class).getStartEndByHourShift(payload.getLocationIdList().get(0), payload.getFromDate(), payload.getToDate());
            payload.setFromDate(startEndByShift.getFirst());
            payload.setToDate(startEndByShift.getSecond());
        } else {
            if(payload.getFromDate() != null){ payload.setFromDate(payload.getFromDate() + "00"); }
            if(payload.getToDate() != null){ payload.setToDate(payload.getToDate() + "00"); }
        }
        List<MachineDowntimeAlertData> machineDowntimeAlertDataList = machineDowntimeAlertRepository.getMachineDowntime(payload, pageable);
        List<Long> machineDowntimeAlertIdList = machineDowntimeAlertDataList.stream().map(MachineDowntimeAlertData::getId).collect(Collectors.toList());
        List<MachineDowntimeReason> machineDowntimeReasonList = machineDowntimeReasonRepository.findAllByMachineDowntimeAlertIdIn(machineDowntimeAlertIdList);
        Map<Long, List<MachineDowntimeReason>> machineDowntimeReasonMap = machineDowntimeReasonList.stream().collect(Collectors.groupingBy(MachineDowntimeReason::getMachineDowntimeAlertId));
        long total = machineDowntimeAlertRepository.countMachineDowntime(payload, pageable);

        machineDowntimeAlertDataList = machineDowntimeAlertDataList.stream().map(machineDowntimeAlertData -> {
            machineDowntimeAlertData.setStartTimeStr( machineDowntimeAlertData.getStartTime() == null ? null :
                    DateUtils2.format(machineDowntimeAlertData.getStartTime(),
                            DateUtils2.DatePattern.yyyy_MM_dd_HH_mm_ss, LocationUtils.getZoneIdByMold(
                                    machineDowntimeAlertData.getMold()))
            );

            Instant fromHour = DateUtils2.toInstant(payload.getFromDate(), DateUtils2.DatePattern.yyyyMMddHH, LocationUtils.getZoneIdByMold(machineDowntimeAlertData.getMold()));
            Instant toHour = DateUtils2.toInstant(payload.getToDate(), DateUtils2.DatePattern.yyyyMMddHH, LocationUtils.getZoneIdByMold(machineDowntimeAlertData.getMold()));
            String until = machineOeeRepository.findFirstTimeByMachineIdAndPeriodTypeOrderByHourDesc(machineDowntimeAlertData.getMachineId());
            Instant untilInstant = StringUtils.isEmpty(until) || until.length() < 14 ? Instant.now() : DateUtils2.toInstant(until, DateUtils2.DatePattern.yyyyMMddHHmmss, LocationUtils.getZoneIdByMold(machineDowntimeAlertData.getMold()));
            if (fromHour == null || toHour == null || !DateUtils.inRange(untilInstant, fromHour, toHour)) {
                untilInstant = Instant.now();
            }

            List<MachineDowntimeReason> machineDowntimeReasonItemList = machineDowntimeReasonMap.get(machineDowntimeAlertData.getId());
            if (CollectionUtils.isNotEmpty(machineDowntimeReasonItemList)) {
                machineDowntimeAlertData.setMachineDowntimeReasonList(machineDowntimeReasonItemList);
            }
            if (machineDowntimeAlertData.getStartTime() != null && machineDowntimeAlertData.getEndTime() != null && machineDowntimeAlertData.getEndTime().compareTo(untilInstant) <= 0) {
                Duration res = Duration.between(machineDowntimeAlertData.getStartTime(), machineDowntimeAlertData.getEndTime());
                machineDowntimeAlertData.setDuration(res.getSeconds());
                machineDowntimeAlertData.setEndTimeStr(DateUtils2.format(machineDowntimeAlertData.getEndTime(),
                    DateUtils2.DatePattern.yyyy_MM_dd_HH_mm_ss, LocationUtils.getZoneIdByMold(
                            machineDowntimeAlertData.getMold())));
            } else if (machineDowntimeAlertData.getStartTime() != null) {
                Duration res = Duration.between(machineDowntimeAlertData.getStartTime(), untilInstant);
                machineDowntimeAlertData.setDuration(res.getSeconds());
                if (toHour != null && untilInstant.isBefore(toHour))
                toHour = untilInstant;
            } else {
                machineDowntimeAlertData.setDuration(0L);
            }

            if (fromHour != null && toHour != null)
            fillSelectedDayDowntime(machineDowntimeAlertData, fromHour, toHour, untilInstant);
            return machineDowntimeAlertData;
        }).collect(Collectors.toList());
        return new PageImpl<>(machineDowntimeAlertDataList, pageable, total);
    }

    private void fillSelectedDayDowntime(MachineDowntimeAlertData alert, Instant fromHour, Instant toHour, Instant until) {
        Duration timeElapsed;
        if (alert.getEndTime() ==  null) {
            if (until.isBefore(fromHour)) {
                timeElapsed = Duration.ofSeconds(0);
            } else if (alert.getStartTime().compareTo(fromHour) <= 0) {
                timeElapsed = Duration.between(fromHour, toHour);
            } else {
                timeElapsed = Duration.between(alert.getStartTime(), toHour);
            }
        } else {
            if (alert.getStartTime().compareTo(fromHour) <= 0 && alert.getEndTime().compareTo(toHour) >= 0) {
                timeElapsed = Duration.between(fromHour, toHour);
            } else if (alert.getStartTime().compareTo(fromHour) >= 0 && alert.getEndTime().compareTo(toHour) <= 0) {
                timeElapsed = Duration.between(alert.getStartTime(), alert.getEndTime());
            } else if (alert.getStartTime().compareTo(fromHour) <= 0 && alert.getEndTime().compareTo(toHour) <= 0) {
                timeElapsed = Duration.between(fromHour, alert.getEndTime());
            } else if (alert.getStartTime().compareTo(fromHour) >= 0 && alert.getEndTime().compareTo(toHour) >= 0) {
                timeElapsed = Duration.between(alert.getStartTime(), toHour);
            } else {
                timeElapsed = Duration.ofSeconds(0);
            }
        }

        alert.setFilteredDuration(timeElapsed.getSeconds());
    }

    public ApiResponse getDowntimeReason(Long alertId) {
        try {
            List<MachineDowntimeReason> reasons = machineDowntimeReasonRepository.findByMachineDowntimeAlertId(alertId);
            return ApiResponse.success(CommonMessage.OK, reasons);
        } catch (Exception e) {
            e.printStackTrace();
            return ApiResponse.error();
        }
    }

    public ApiResponse updateDowntimeReason(MachineDowntimeReasonData data) {
        try {
            //update alert status
            MachineDowntimeAlert alert = machineDowntimeAlertRepository.getOne(data.getMachineDowntimeAlertId());

            if (alert.getDowntimeStatus().equals(MachineDowntimeAlertStatus.DOWNTIME)
                    && (alert.getEndTime() == null || alert.getEndTime().compareTo(Instant.now()) > 0)) {
                alert.setDowntimeStatus(MachineDowntimeAlertStatus.REGISTERED);
                alert.setReportedBy(SecurityUtils.getUserId());
            }
            if (MachineDowntimeAlertStatus.CONFIRMED.equals(data.getStatus())) {
                alert.setDowntimeStatus(MachineDowntimeAlertStatus.CONFIRMED);
                alert.setConfirmedBy(SecurityUtils.getUserId());
            }
            if (data.getDowntimeType() != null)
                alert.setDowntimeType(data.getDowntimeType());
            alert.setLatest(true);
            machineDowntimeAlertRepository.save(alert);

//            delete old reasons
            List<MachineDowntimeReason> oldReasons = machineDowntimeReasonRepository.findByMachineDowntimeAlertId(data.getMachineDowntimeAlertId());
            machineDowntimeReasonRepository.deleteAll(oldReasons);

            //save new reasons
            List<MachineDowntimeReason> reasons = bindData(data);
            machineDowntimeReasonRepository.saveAll(reasons);
            return ApiResponse.success(CommonMessage.OK, reasons);
        } catch (Exception e) {
            e.printStackTrace();
            return ApiResponse.error(CommonMessage.SOMETHING_WENT_WRONG);
        }
    }

    public ApiResponse confirmAlert(MachineDowntimeReasonData data) {
        try {
            //update reason with alert status = CONFIRMED
            data.setStatus(MachineDowntimeAlertStatus.CONFIRMED);
            updateDowntimeReason(data);
            return ApiResponse.success(CommonMessage.OK, machineDowntimeAlertRepository.getOne(data.getMachineDowntimeAlertId()));
        } catch (Exception e) {
            e.printStackTrace();
            return ApiResponse.error();
        }
    }

    private List<MachineDowntimeReason> bindData(MachineDowntimeReasonData data) {
        List<MachineDowntimeReason> result = new ArrayList<>();
        data.getItems().forEach(item -> result.add(new MachineDowntimeReason(data.getMachineDowntimeAlertId(), item)));
        return result;
    }

    public ApiResponse getMachineDowntimeAlertIds(SearchMachineDowntimePayload payload, Pageable pageable) {
        return ApiResponse.success(CommonMessage.OK, machineDowntimeAlertRepository.getAllIds(payload, pageable));
    }

    public ApiResponse migrateMoldId(){
        List<MachineDowntimeAlert> alerts = machineDowntimeAlertRepository.findAll();
        List<ThreeObject<Long, Long, Long>> result = new ArrayList<>();
        alerts.forEach(a -> {
            String startHour = DateUtils2.format(a.getStartTime(), DateUtils2.DatePattern.yyyyMMddHH, DateUtils2.Zone.SYS);
            String endHour = DateUtils2.format((a.getEndTime() != null ? a.getEndTime() : Instant.now()), DateUtils2.DatePattern.yyyyMMddHH, DateUtils2.Zone.SYS);
            List<MachineOee> machineOeeList =  BeanUtils.get(MachineOeeRepository.class).findAllByMachineIdAndHourBetweenAndPeriodType(a.getMachineId(), startHour, endHour, Frequent.HOURLY);
            Mold mold = machineOeeList.stream().map(MachineOee::getMold).filter(Objects::nonNull).findFirst().orElse(a.getMachine().getMold());
            if (mold != null) {
                a.setMoldId(mold.getId());
            }

            result.add(new ThreeObject<>(a.getId(), machineOeeList.stream().map(MachineOee::getId).findFirst().orElse(null), a.getMoldId()));
        });
        return ApiResponse.success(CommonMessage.OK, result);
    }
}
