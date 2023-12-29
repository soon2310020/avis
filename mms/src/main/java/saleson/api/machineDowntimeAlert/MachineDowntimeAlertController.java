package saleson.api.machineDowntimeAlert;

import com.emoldino.api.common.resource.base.accesscontrol.annotation.DataLeakDetector;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import saleson.api.machineDowntimeAlert.data.MachineDowntimeReasonData;
import saleson.api.machineDowntimeAlert.payload.MachineDowntimeAlertData;
import saleson.api.machineDowntimeAlert.payload.SearchMachineDowntimePayload;
import saleson.common.payload.ApiResponse;


@RestController
@RequestMapping("/api/machine-downtime-alert")
@Slf4j
public class MachineDowntimeAlertController {

    @Autowired
    private MachineDowntimeAlertService machineDowntimeAlertService;

    @DataLeakDetector(disabled = true)
    @GetMapping
    public ResponseEntity<Page<MachineDowntimeAlertData>> getMachineDowntimeAlert(SearchMachineDowntimePayload payload, Pageable pageable) {
        return ResponseEntity.ok(machineDowntimeAlertService.getMachineDowntimeAlert(payload,pageable));
    }

    @GetMapping("/reasons/{id}")
    public ApiResponse getDowntimeReason(@PathVariable("id") Long id) {
        return machineDowntimeAlertService.getDowntimeReason(id);
    }

    @PostMapping("/reasons/update")
    public ApiResponse updateDowntimeReason(@RequestBody MachineDowntimeReasonData data) {
        return machineDowntimeAlertService.updateDowntimeReason(data);
    }

    @PostMapping("/confirm")
    public ApiResponse confirmAlert(@RequestBody MachineDowntimeReasonData data) {
        return machineDowntimeAlertService.confirmAlert(data);
    }

    @GetMapping("/migrate-mold-id")
    public ApiResponse migrateMoldId() {
        return machineDowntimeAlertService.migrateMoldId();
    }
}
