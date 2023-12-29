package saleson.api.shiftConfig;

import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import saleson.api.shiftConfig.payload.ShiftConfigData;
import saleson.common.payload.ApiResponse;

@RestController
@RequestMapping("/api/shift-config")
@Slf4j
public class ShiftConfigController {
    @Autowired
    private ShiftConfigService shiftConfigService;

    @GetMapping
    public ApiResponse get(@RequestParam("locationId") Long locationId) {
        return shiftConfigService.get(locationId);
    }

    @PostMapping("/save")
    public ApiResponse save(@RequestBody ShiftConfigData data) {
        return shiftConfigService.save(data);
    }

    @GetMapping("/get-by-location-and-day")
    public ApiResponse getByLocationAndDay(@RequestParam("locationId") Long locationId,
                                           @RequestParam("day") String day){
        return shiftConfigService.getShiftListByLocationAndDay(locationId, day);
    }
}
