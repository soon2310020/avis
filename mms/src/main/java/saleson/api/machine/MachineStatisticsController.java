package saleson.api.machine;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import saleson.api.machine.payload.AvgOEE;
import saleson.api.machine.payload.DetailOEE;
import saleson.api.machine.payload.MachineStatisticsPayload;
import saleson.api.machine.payload.OeePayload;
import saleson.common.enumeration.MachineAvailabilityType;
import saleson.common.payload.ApiResponse;
import saleson.model.RiskLevel;
import saleson.model.data.MachineStatisticsData;

import java.util.List;

@RestController
@RequestMapping("/api/machines/statistics")
@Slf4j
public class MachineStatisticsController {
    @Autowired
    private MachineStatisticsService machineStatisticsService;


    @GetMapping("/all")
    public ResponseEntity<Page<MachineStatisticsData>> getAllMaChineStatistics(MachineStatisticsPayload payload, Pageable pageable, Model model) {
        Page<MachineStatisticsData> pageContent = machineStatisticsService.getAllMachineAvailabilityConfig(payload.getPredicate(), pageable, payload.getDay());
        model.addAttribute("pageContent", pageContent);
        return new ResponseEntity<>(pageContent, HttpStatus.OK);
    }

    @GetMapping("/get-one")
    public ResponseEntity<?> getOneMachineStatistics(@RequestParam("machineId") Long machineId, @RequestParam("day") String day) {
        return ResponseEntity.ok(machineStatisticsService.getMachineStatisticsDataByDay(machineId, day));
    }

    @GetMapping("/check-existed-config")
    public Boolean checkExistedConfig(@RequestParam("start") String start,
                                      @RequestParam("end") String end,
                                      @RequestParam("machineId") Long machineId,
                                      @RequestParam("type") MachineAvailabilityType type) {
        return machineStatisticsService.checkExistedConfig(start, end, machineId, type);
    }

    @PostMapping("/update-config")
    public ResponseEntity<?> updateMachineConfigs(@RequestBody MachineStatisticsPayload payload,
                                                  @RequestParam("type") MachineAvailabilityType type) {
        return ResponseEntity.ok(machineStatisticsService.updateConfig(payload, type));
    }

    @PostMapping("/check-conflict")
    public ApiResponse checkConflict(@RequestBody MachineStatisticsPayload payload,
                                     @RequestParam("type") MachineAvailabilityType type) {
        return machineStatisticsService.checkConflict(type, payload);
    }


    @GetMapping("/oee")
    public ResponseEntity<AvgOEE> getAllOEE(@RequestParam("start") String start,
                                            @RequestParam("end") String end,
                                            @RequestParam("companyId") Long companyId,
                                            @RequestParam("line") String line,
                                            Pageable pageable) {
        return ResponseEntity.ok(machineStatisticsService.getOverallEquipmentEffectiveness(start, end, companyId, line, pageable));
    }


    @GetMapping("/oee/overview")
    public ApiResponse getOeeOverview(OeePayload payload) {
        return machineStatisticsService.getOeeOverview_New(payload);
    }


    @GetMapping("/oee/part-produced")
    public ApiResponse getOeeDetail(OeePayload payload, Pageable pageable) {
        return machineStatisticsService.getOeeDetail(payload, pageable);
    }


    @GetMapping("/oee-details")
    public Page<DetailOEE> getOEEDetails(@RequestParam("start") String start,
                                         @RequestParam("end") String end,
                                         @RequestParam("companyId") Long companyId,
                                         @RequestParam("line") String line,
                                         Pageable pageable) {
        return machineStatisticsService.getOverallEquipmentEffectivenessDetails(start, end, companyId, line, pageable);
    }

    @PostMapping("/update-risk-level")
    public ResponseEntity<List<RiskLevel>> updateRiskLevel(@RequestBody List<RiskLevel> riskLevels) {
        return ResponseEntity.ok(machineStatisticsService.updateRiskLevel(riskLevels));
    }

    @GetMapping("/get-risk-level")
    public ResponseEntity<List<RiskLevel>> getRiskLevel() {
        return ResponseEntity.ok(machineStatisticsService.getAllRiskLevel());
    }

    @GetMapping("/update-oee-by-day-range")
    public ApiResponse updateOEEByDay(@RequestParam("from") String from,
                                      @RequestParam("to") String to) {
        machineStatisticsService.updateOEEByDayRange(from, to);
        return ApiResponse.success();
    }

    @GetMapping("/migrate-old-downtime-data")
    public ApiResponse migrateOldDowntimeData() {
        return machineStatisticsService.migrateOldDowntimeData();
    }

    @GetMapping("/fix-oee-data")
    public ApiResponse fixAvailability() {
        return machineStatisticsService.fixAvailability();
    }

    @GetMapping("/oee/cover-old-data")
    public ApiResponse coverOldData() {
        return machineStatisticsService.coverOldData();
    }

    @GetMapping("/migrate-machine-rejected-part")
    public ApiResponse migrateMachineRejectedPart() {
        return machineStatisticsService.migrateMachineRejectedPart();
    }

    @GetMapping("/migrate-old-machine-history-time")
    public ApiResponse migrateOldMachineHistoryTime(){
        return machineStatisticsService.migrateOldMachineHistoryTime();
    }
}
