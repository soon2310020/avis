package saleson.api.rejectedPart;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import saleson.api.rejectedPart.payload.RejectRatePayload;
import saleson.api.rejectedPart.payload.RejectedPartPayload;
import saleson.common.constant.CommonMessage;
import saleson.common.payload.ApiResponse;
import saleson.dto.RejectRateOEEDTO;
import saleson.model.rejectedPartRate.ProducedPart;

import java.util.List;

@RestController
@RequestMapping("/api/rejected-part")
public class RejectedPartController {
    @Autowired
    RejectedPartService rejectedPartService;

    @GetMapping
    public ResponseEntity<Page<ProducedPart>> getProducedPart(RejectedPartPayload payload, Pageable pageable, Model model){
//        if(payload.getStartDate() == null || payload.getEndDate() == null){
//            payload.setStartDate(DateUtils.getToday("yyyyMMdd"));
//            payload.setEndDate(DateUtils.getToday("yyyyMMdd"));
//        }

        Page<ProducedPart> pageContent = rejectedPartService.findAll(payload, pageable);

        model.addAttribute("pageContent", pageContent);
        return new ResponseEntity<>(pageContent, HttpStatus.OK);
    }

    @PostMapping("/register")
    public ResponseEntity<?> setRejectedParts(@RequestBody List<RejectedPartPayload> payload) {
        return new ResponseEntity<>(rejectedPartService.setRejectedParts(payload), HttpStatus.OK);
    }

    @GetMapping("/breakdown-chart")
    public ResponseEntity<?> getBreakdownData(RejectedPartPayload payload){
        return new ResponseEntity<>(rejectedPartService.getBreakdownData(payload), HttpStatus.OK);
    }

    @PostMapping
    public String test(@RequestParam Long statisticsId, @RequestParam Long moldId){
        rejectedPartService.test(statisticsId, moldId);
        return "OK";
    }

    @GetMapping("/get-reject-rate-reason")
    public ApiResponse getRejectRateReason(@RequestParam(value = "id") Long id) {
        return rejectedPartService.getRejectRateReason(id);
    }

    @GetMapping("/get-reject-rate-oee")
    public ResponseEntity<Page<RejectRateOEEDTO>> getRejectRateOee(RejectRatePayload payload, Pageable pageable){
        return ResponseEntity.ok(rejectedPartService.getRejectRateOee(payload, pageable));
    }

    @GetMapping("/fix-produced-part-data")
    public ApiResponse fixProducedPartData() {
        return rejectedPartService.fixProducedPartData();
    }

    @GetMapping("/set-configuration")
    public ApiResponse setConfiguration(@RequestParam("frequent") String frequent) {
        return rejectedPartService.setConfiguration(frequent);
    }

    @GetMapping("/get-configuration")
    public ApiResponse getConfiguration(){
        return rejectedPartService.getConfiguration();
    }

    @GetMapping("/get-by-hour-and-machine")
    public ApiResponse getByHourAndMachine(@RequestParam("machineId") Long machineId,
                                           @RequestParam("hour") String hour) {
        return rejectedPartService.getByHourAndMachine(machineId, hour);
    }

    @GetMapping(value = "/get-reject-part-entry-record")
    public ApiResponse getRejectPartEntryRecord(@RequestParam(value = "machineId") Long machineId, @RequestParam(value = "day") String day, Pageable pageable) {
        return ApiResponse.success(CommonMessage.OK, rejectedPartService.getRejectPartEntryRecord(machineId, day, pageable));
    }

    @GetMapping(value = "/get-reject-part-summary")
    public ApiResponse getRejectPartSummary(@RequestParam(value = "machineId") Long machineId, @RequestParam(value = "day") String day, Pageable pageable) {
        return ApiResponse.success(CommonMessage.OK, rejectedPartService.getRejectPartSummary(machineId, day, pageable));
    }
}
