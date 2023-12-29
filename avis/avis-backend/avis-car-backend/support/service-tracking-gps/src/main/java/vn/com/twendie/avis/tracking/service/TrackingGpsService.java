package vn.com.twendie.avis.tracking.service;

import org.apache.commons.lang3.BooleanUtils;
import org.javatuples.Pair;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.util.CollectionUtils;
import vn.com.twendie.avis.api.core.util.DateUtils;
import vn.com.twendie.avis.tracking.model.tracking.VehicleActivity;
import vn.com.twendie.avis.tracking.model.tracking.VehicleFeeStation;
import vn.com.twendie.avis.tracking.model.tracking.VehicleStop;
import vn.com.twendie.avis.tracking.model.tracking.VehicleWorkingTime;

import java.math.BigDecimal;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static java.math.BigDecimal.ZERO;
import static vn.com.twendie.avis.api.core.util.DateUtils.UTC_TIME_ZONE;

public interface TrackingGpsService {

    DateUtils DATE_UTILS = new DateUtils();

    default String cleanVehiclePlate(String vehiclePlate) {
        return vehiclePlate.replaceAll("[\\s\\-]", "");
    }

    List<VehicleActivity> getVehicleActivities(String vehiclePlate, Timestamp from, Timestamp to) throws Exception;

    @Cacheable(cacheNames = "VehicleActivity",
            key = "#vehiclePlate.concat('-').concat(#date.toString())",
            condition = "#date.before(new vn.com.twendie.avis.api.core.util.DateUtils().startOfToday())")
    default List<VehicleActivity> getVehicleActivities(String vehiclePlate, Timestamp date) throws Exception {
        return getVehicleActivities(vehiclePlate, DATE_UTILS.startOfDay(date), DATE_UTILS.endOfDay(date));
    }

    default VehicleWorkingTime getVehicleWorkingTime(List<VehicleActivity> vehicleActivities) {
        if (Objects.nonNull(vehicleActivities)) {
            vehicleActivities = vehicleActivities.stream()
                    .filter(vehicleActivity -> !BooleanUtils.isTrue(vehicleActivity.getIsSupplemental()))
                    .collect(Collectors.toList());
            if (!vehicleActivities.isEmpty()) {
                Time from = DATE_UTILS.getTime(vehicleActivities.get(0).getStartTime(), UTC_TIME_ZONE);
                Time to = DATE_UTILS.getTime(vehicleActivities.get(vehicleActivities.size() - 1).getEndTime(), UTC_TIME_ZONE);
                return new VehicleWorkingTime(from, to);
            }
        }
        return new VehicleWorkingTime();
    }

    default List<Pair<String, String>> getVehicleTripItinerary(List<VehicleActivity> vehicleActivities) {
        if (CollectionUtils.isEmpty(vehicleActivities)) {
            return new ArrayList<>();
        } else {
            return vehicleActivities.stream()
                    .filter(vehicleActivity -> !BooleanUtils.isTrue(vehicleActivity.getIsSupplemental()))
                    .map(vehicleActivity -> new ArrayList<Pair<String, String>>() {{
                        add(Pair.with(vehicleActivity.getStartDistrict(), vehicleActivity.getStartProvince()));
                        add(Pair.with(vehicleActivity.getEndDistrict(), vehicleActivity.getEndProvince()));
                    }})
                    .flatMap(Collection::stream)
                    .collect(Collectors.toList());
        }
    }

    default BigDecimal getTotalKmGps(List<VehicleActivity> vehicleActivities) {
        return vehicleActivities.stream()
                .map(VehicleActivity::getKmGPS)
                .reduce(BigDecimal::add)
                .orElse(ZERO);
    }

    List<VehicleStop> getVehicleStops(String vehiclePlate, Timestamp from, Timestamp to) throws Exception;

    @Cacheable(cacheNames = "VehicleStop",
            key = "#vehiclePlate.concat('-').concat(#date.toString())",
            condition = "#date.before(new vn.com.twendie.avis.api.core.util.DateUtils().startOfToday())")
    default List<VehicleStop> getVehicleStops(String vehiclePlate, Timestamp date) throws Exception {
        return getVehicleStops(vehiclePlate, DATE_UTILS.startOfDay(date), DATE_UTILS.endOfDay(date));
    }

    List<VehicleFeeStation> getVehicleFeeStations(String vehiclePlate, Timestamp from, Timestamp to) throws Exception;

    @Cacheable(cacheNames = "VehicleFeeStation",
            key = "#vehiclePlate.concat('-').concat(#date.toString())",
            condition = "#date.before(new vn.com.twendie.avis.api.core.util.DateUtils().startOfToday())")
    default List<VehicleFeeStation> getVehicleFeeStations(String vehiclePlate, Timestamp date) throws Exception {
        return getVehicleFeeStations(vehiclePlate, DATE_UTILS.startOfDay(date), DATE_UTILS.endOfDay(date));
    }

}
