package vn.com.twendie.avis.tracking.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import vn.com.twendie.avis.api.core.util.DateUtils;
import vn.com.twendie.avis.tracking.model.payload.VehicleActivityPayload;
import vn.com.twendie.avis.tracking.model.payload.VehicleFeeStationPayload;
import vn.com.twendie.avis.tracking.model.payload.VehicleStopPayload;
import vn.com.twendie.avis.tracking.model.tracking.VehicleActivity;
import vn.com.twendie.avis.tracking.model.tracking.VehicleFeeStation;
import vn.com.twendie.avis.tracking.model.tracking.VehicleStop;
import vn.com.twendie.avis.tracking.repository.TrackingGpsRepo;
import vn.com.twendie.avis.tracking.service.TrackingGpsService;

import java.sql.Timestamp;
import java.util.List;

import static vn.com.twendie.avis.api.core.util.DateUtils.LOCAL_TIME_ZONE;
import static vn.com.twendie.avis.api.core.util.DateUtils.T_MEDIUM_PATTERN;
import static vn.com.twendie.avis.tracking.model.payload.VehicleStopPayload.MIN_STOP_TIME;

@Service
@Slf4j
public class TrackingGpsServiceImpl implements TrackingGpsService {

    private final TrackingGpsRepo trackingGpsRepo;

    private final DateUtils dateUtils;

    public TrackingGpsServiceImpl(TrackingGpsRepo trackingGpsRepo, DateUtils dateUtils) {
        this.trackingGpsRepo = trackingGpsRepo;
        this.dateUtils = dateUtils;
    }

    @Override
    @Cacheable(cacheNames = "VehicleActivity",
            key = "#vehiclePlate.concat('-').concat(#from.toString()).concat('-').concat(#to.toString())",
            condition = "#to.before(new vn.com.twendie.avis.api.core.util.DateUtils().now())")
    public List<VehicleActivity> getVehicleActivities(String vehiclePlate, Timestamp from, Timestamp to) throws Exception {
        VehicleActivityPayload payload = VehicleActivityPayload.builder()
                .vehiclePlate(cleanVehiclePlate(vehiclePlate))
                .fromDate(dateUtils.format(from, T_MEDIUM_PATTERN, LOCAL_TIME_ZONE))
                .toDate(dateUtils.format(to, T_MEDIUM_PATTERN, LOCAL_TIME_ZONE))
                .build();
        log.info("FROM: " + dateUtils.format(from, T_MEDIUM_PATTERN, LOCAL_TIME_ZONE)+"------------"+ from.toString());
        log.info("TO: "+ dateUtils.format(to, T_MEDIUM_PATTERN, LOCAL_TIME_ZONE) +"---------------" +to.toString());
        return trackingGpsRepo.getVehicleActivities(payload).getVehicleActivities();
    }

    @Override
    @Cacheable(cacheNames = "VehicleStop",
            key = "#vehiclePlate.concat('-').concat(#from.toString()).concat('-').concat(#to.toString())",
            condition = "#to.before(new vn.com.twendie.avis.api.core.util.DateUtils().now())")
    public List<VehicleStop> getVehicleStops(String vehiclePlate, Timestamp from, Timestamp to) throws Exception {
        VehicleStopPayload payload = VehicleStopPayload.builder()
                .vehiclePlate(cleanVehiclePlate(vehiclePlate))
                .fromDate(dateUtils.format(from, T_MEDIUM_PATTERN, LOCAL_TIME_ZONE))
                .toDate(dateUtils.format(to, T_MEDIUM_PATTERN, LOCAL_TIME_ZONE))
                .totalTime(MIN_STOP_TIME)
                .build();
        return trackingGpsRepo.getVehicleStops(payload).getVehicleStops();
    }

    @Override
    public List<VehicleFeeStation> getVehicleFeeStations(String vehiclePlate, Timestamp from, Timestamp to) throws Exception {
        VehicleFeeStationPayload payload = VehicleFeeStationPayload.builder()
                .vehiclePlate(cleanVehiclePlate(vehiclePlate))
                .fromDate(dateUtils.format(from, T_MEDIUM_PATTERN, LOCAL_TIME_ZONE))
                .toDate(dateUtils.format(to, T_MEDIUM_PATTERN, LOCAL_TIME_ZONE))
                .build();
        return trackingGpsRepo.getVehicleFeeStations(payload).getVehicleFeeStations();
    }

}
