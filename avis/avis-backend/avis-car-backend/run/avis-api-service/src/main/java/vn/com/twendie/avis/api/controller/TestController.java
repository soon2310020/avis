package vn.com.twendie.avis.api.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import vn.com.twendie.avis.api.core.util.DateUtils;
import vn.com.twendie.avis.api.rest.model.ApiResponse;
import vn.com.twendie.avis.security.annotation.RequirePermission;
import vn.com.twendie.avis.tracking.model.tracking.VehicleActivity;
import vn.com.twendie.avis.tracking.service.TrackingGpsService;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("/test")
@Slf4j
public class TestController {

    @Autowired
    private TrackingGpsService trackingGpsService;

    @Autowired
    private DateUtils dateUtils;

    @GetMapping("/gps")
    @RequirePermission(acceptedRoles = {"Superuser"})
    public ApiResponse<?> getGPS(@RequestParam(name = "number_plate") String numberPlate,
                                 @RequestParam(name = "from_date", required = false) Long fromDate,
                                 @RequestParam(name = "to_date", required = false) Long toDate) throws Exception {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");

        List<VehicleActivity> vehicleActivityList = null;
        if (Objects.nonNull(fromDate) && Objects.nonNull(toDate)) {
            vehicleActivityList = trackingGpsService.getVehicleActivities(numberPlate,
                    dateUtils.startOfDay(fromDate),
                    dateUtils.endOfDay(toDate));
        } else {
            vehicleActivityList = trackingGpsService.getVehicleActivities(numberPlate, dateUtils.now());
        }

        log.info("{}", vehicleActivityList);

        return ApiResponse.success(vehicleActivityList);
    }
}
