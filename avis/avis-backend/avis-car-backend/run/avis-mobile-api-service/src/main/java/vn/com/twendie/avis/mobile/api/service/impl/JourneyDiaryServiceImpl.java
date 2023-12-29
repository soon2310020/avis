package vn.com.twendie.avis.mobile.api.service.impl;

import com.google.common.collect.Sets;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import vn.com.twendie.avis.api.core.util.DateUtils;
import vn.com.twendie.avis.api.core.util.ListUtils;
import vn.com.twendie.avis.api.rest.exception.NotFoundException;
import vn.com.twendie.avis.data.model.JourneyDiary;
import vn.com.twendie.avis.data.model.JourneyDiaryCostType;
import vn.com.twendie.avis.data.model.JourneyDiaryStationFee;
import vn.com.twendie.avis.mobile.api.adapter.JourneyDiaryTotalInfoAdapter;
import vn.com.twendie.avis.mobile.api.model.response.JourneyDiaryTotalInfo;
import vn.com.twendie.avis.mobile.api.repository.JourneyDiaryCostTypeRepo;
import vn.com.twendie.avis.mobile.api.repository.JourneyDiaryRepo;
import vn.com.twendie.avis.mobile.api.repository.JourneyDiaryStationFeeRepo;
import vn.com.twendie.avis.mobile.api.service.CostTypeService;
import vn.com.twendie.avis.mobile.api.service.JourneyDiaryService;
import vn.com.twendie.avis.tracking.model.tracking.VehicleActivity;
import vn.com.twendie.avis.tracking.model.tracking.VehicleFeeStation;
import vn.com.twendie.avis.tracking.service.TrackingGpsService;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.*;
import java.util.stream.Collectors;

import static java.math.BigDecimal.ZERO;
import static vn.com.twendie.avis.data.enumtype.JourneyDiaryCostTypeEnum.TOLLS_FEE;
import static vn.com.twendie.avis.queue.constant.QueueConstant.RoutingKeys.UPDATE_TOTAL_KM_GPS_JOURNEY_DIARY;

@Slf4j
@Service
public class JourneyDiaryServiceImpl implements JourneyDiaryService {

    private final JourneyDiaryRepo journeyDiaryRepo;
    private final JourneyDiaryCostTypeRepo journeyDiaryCostTypeRepo;
    private final JourneyDiaryStationFeeRepo journeyDiaryStationFeeRepo;

    private final CostTypeService costTypeService;
    private final TrackingGpsService trackingGpsService;

    private final DateUtils dateUtils;
    private final ListUtils listUtils;

    public JourneyDiaryServiceImpl(JourneyDiaryRepo journeyDiaryRepo,
                                   JourneyDiaryCostTypeRepo journeyDiaryCostTypeRepo,
                                   JourneyDiaryStationFeeRepo journeyDiaryStationFeeRepo,
                                   CostTypeService costTypeService,
                                   TrackingGpsService trackingGpsService,
                                   DateUtils dateUtils,
                                   ListUtils listUtils) {
        this.journeyDiaryRepo = journeyDiaryRepo;
        this.journeyDiaryCostTypeRepo = journeyDiaryCostTypeRepo;
        this.journeyDiaryStationFeeRepo = journeyDiaryStationFeeRepo;
        this.costTypeService = costTypeService;
        this.trackingGpsService = trackingGpsService;
        this.dateUtils = dateUtils;
        this.listUtils = listUtils;
    }

    @Override
    public JourneyDiary findById(Long id) {
        if (Objects.nonNull(id)) {
            return journeyDiaryRepo.findByIdAndDeletedFalse(id)
                    .orElseThrow(() -> new NotFoundException("Not found journey diary with id: " + id));
        } else {
            return null;
        }
    }

    @Override
    public JourneyDiaryTotalInfo getJourneyDiaryTotalInfo(Long id) {
        JourneyDiary journeyDiary = journeyDiaryRepo.findById(id)
                .orElseThrow(() -> new NotFoundException("Not found journey diary with id: " + id)
                        .code(HttpStatus.NOT_FOUND.value()));
        updateJourneyDiaryStationFees(journeyDiary);
        return new JourneyDiaryTotalInfoAdapter().apply(journeyDiary);
    }

    @Override
    public JourneyDiary save(JourneyDiary journeyDiary) {
        return journeyDiaryRepo.save(journeyDiary);
    }

    @Override
    public List<JourneyDiary> findByIdInAndStep(List<Long> ids, long step) {
        return null;
    }

    @Override
    public void fetchJourneyDiaryCostTypes(Collection<JourneyDiary> journeyDiaries) {
        if (!CollectionUtils.isEmpty(journeyDiaries)) {
            Set<Long> journeyDiaryIds = journeyDiaries.stream()
                    .map(JourneyDiary::getId)
                    .collect(Collectors.toSet());
            List<JourneyDiaryCostType> journeyDiaryCostTypes = journeyDiaryCostTypeRepo
                    .findByJourneyDiaryIdInAndDeletedFalse(journeyDiaryIds);
            Map<Long, Set<JourneyDiaryCostType>> journeyDiaryCostTypesMap = journeyDiaryCostTypes.stream()
                    .collect(Collectors.groupingBy(JourneyDiaryCostType::getJourneyDiaryId, Collectors.toSet()));
            journeyDiaries.forEach(journeyDiary -> journeyDiary.setJourneyDiaryCostTypes(
                    journeyDiaryCostTypesMap.getOrDefault(journeyDiary.getId(), Sets.newHashSet())
            ));
        }
    }

    @Override
    public void fetchJourneyDiaryStationFees(Collection<JourneyDiary> journeyDiaries) {
        if (!CollectionUtils.isEmpty(journeyDiaries)) {
            Set<Long> journeyDiaryIds = journeyDiaries.stream()
                    .map(JourneyDiary::getId)
                    .collect(Collectors.toSet());
            List<JourneyDiaryStationFee> journeyDiaryStationFees = journeyDiaryStationFeeRepo
                    .findByJourneyDiaryIdInAndDeletedFalse(journeyDiaryIds);
            Map<Long, Set<JourneyDiaryStationFee>> journeyDiaryStationFeesMap = journeyDiaryStationFees.stream()
                    .collect(Collectors.groupingBy(JourneyDiaryStationFee::getJourneyDiaryId, Collectors.toSet()));
            journeyDiaries.forEach(journeyDiary -> journeyDiary.setJourneyDiaryStationFees(
                    journeyDiaryStationFeesMap.getOrDefault(journeyDiary.getId(), Sets.newHashSet())
            ));
        }
    }

    @Override
    @RabbitListener(queues = UPDATE_TOTAL_KM_GPS_JOURNEY_DIARY)
    public void updateTotalKmGps(Long journeyDiaryId) {
        try {
            Thread.sleep(10000);
            JourneyDiary journeyDiary = findById(journeyDiaryId);
            updateTotalKmGps(journeyDiary);
        } catch (Exception e) {
            log.error("Error total km gps journey_diary id {}: {}", journeyDiaryId, ExceptionUtils.getRootCauseMessage(e));
        }
    }

    @Override
    public void updateTotalKmGps(JourneyDiary journeyDiary) throws Exception {
        Timestamp timeStart = journeyDiary.getTimeStart();
        Timestamp timeEnd = journeyDiary.getTimeEnd();
        if (ObjectUtils.allNotNull(timeStart, timeEnd)) {
            List<VehicleActivity> vehicleActivities = trackingGpsService
                    .getVehicleActivities(journeyDiary.getVehicle().getNumberPlate(), timeStart, timeEnd);
            BigDecimal totalKmGps = trackingGpsService.getTotalKmGps(vehicleActivities);
            journeyDiary.setTotalKmGps(totalKmGps);
            save(journeyDiary);
        }
    }

    @Override
    public void updateJourneyDiaryStationFees(JourneyDiary journeyDiary) {
        try {
            fetchJourneyDiaryStationFees(journeyDiary);
            Set<Timestamp> timestamps = journeyDiary.getJourneyDiaryStationFees()
                    .stream()
                    .map(JourneyDiaryStationFee::getInTime)
                    .collect(Collectors.toSet());
            List<VehicleFeeStation> vehicleFeeStations = trackingGpsService
                    .getVehicleFeeStations(journeyDiary.getVehicle().getNumberPlate(),
                            journeyDiary.getTimeStart(),
                            ObjectUtils.defaultIfNull(journeyDiary.getTimeEnd(), dateUtils.now()));
            List<JourneyDiaryStationFee> journeyDiaryStationFees = listUtils.mapAll(vehicleFeeStations, JourneyDiaryStationFee.class)
                    .stream()
                    .filter(journeyDiaryStationFee -> !timestamps.contains(journeyDiaryStationFee.getInTime()))
                    .collect(Collectors.toList());
            journeyDiaryStationFees.forEach(journeyDiaryStationFee -> {
                journeyDiaryStationFee.setJourneyDiary(journeyDiary);
                journeyDiaryStationFee.setDriver(journeyDiary.getDriver());
                journeyDiaryStationFee.setVehicle(journeyDiary.getVehicle());
            });
            journeyDiaryStationFees = journeyDiaryStationFeeRepo.saveAll(journeyDiaryStationFees);
            journeyDiary.getJourneyDiaryStationFees().addAll(journeyDiaryStationFees);
            BigDecimal total = journeyDiary.getJourneyDiaryStationFees().stream()
                    .map(JourneyDiaryStationFee::getFee)
                    .filter(Objects::nonNull)
                    .reduce(BigDecimal::add)
                    .orElse(ZERO);
            fetchJourneyDiaryCostTypes(journeyDiary);
            JourneyDiaryCostType tolls_fee = journeyDiary.getJourneyDiaryCostTypes()
                    .stream()
                    .filter(journeyDiaryCostType -> journeyDiaryCostType.getCostType().getCode().equals(TOLLS_FEE.getCode()))
                    .findFirst()
                    .orElse(null);
            if (Objects.nonNull(tolls_fee)) {
                tolls_fee.setValue(total);
            } else {
                tolls_fee = JourneyDiaryCostType.builder()
                        .journeyDiary(journeyDiary)
                        .costType(costTypeService.findByCode(TOLLS_FEE.getCode()))
                        .value(total)
                        .build();
            }
            journeyDiaryCostTypeRepo.save(tolls_fee);
            journeyDiary.getJourneyDiaryCostTypes().add(tolls_fee);
        } catch (Exception ignored) {
        }
    }

}