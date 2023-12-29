package saleson.api.dataCompletionRate;

import com.emoldino.api.common.resource.base.accesscontrol.annotation.DataLeakDetector;
import org.jfree.util.ObjectTable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import saleson.api.dataCompletionRate.payload.*;
import saleson.common.constant.CommonMessage;
import saleson.common.enumeration.ObjectType;
import saleson.common.payload.ApiResponse;
import saleson.model.data.completionRate.AllTypeCompletionRateData;
import saleson.model.data.completionRate.AvgCompletionRateData;

import java.util.List;

@RestController
@RequestMapping("/api/data-completion")
public class DataCompletionController {
    @Autowired
    DataCompletionRateService dataCompletionRateService;

    @DataLeakDetector(disabled = true)
    @GetMapping
    public ResponseEntity getDataCompletionRate(DataCompletionRatePayload payload, Pageable pageable) {
        AvgCompletionRateData data = dataCompletionRateService.getCompletionRateDataListDataLeak(payload, pageable);
        return new ResponseEntity<>(data, HttpStatus.OK);
    }

    @GetMapping("/all-types")
    public ResponseEntity getDataCompletionRateOfAllObjectType(DataCompletionRatePayload payload) {
        AllTypeCompletionRateData data = dataCompletionRateService.getCompletionRateForAllObjectTypes(payload);
        return new ResponseEntity<>(data, HttpStatus.OK);
    }

    @GetMapping("/companies")
    public ResponseEntity getListCompletionRateCompany(DataCompletionRatePayload payload, Pageable pageable) {
        AvgCompletionRateData data = dataCompletionRateService.getCompanyCompletionRate(payload, pageable);
        return new ResponseEntity<>(data, HttpStatus.OK);
    }

    @PostMapping("/update-all")
    public ApiResponse updateAll() {
        return dataCompletionRateService.updateAllCompletionRate();
    }

    @GetMapping("/generate-request_id")
    public String generateRequestID() {
        return dataCompletionRateService.generateRequestID();
    }

    @GetMapping("/list-received-orders")
    public ApiResponse listReceivedOrders() {
        try {
            List<DataCompletionGroupByType> list = dataCompletionRateService.getReceivedCompletionOrders();
            return ApiResponse.success(CommonMessage.OK, list);
        } catch (Exception e) {
            return ApiResponse.error(CommonMessage.SOMETHING_WENT_WRONG);
        }
    }

    @GetMapping("/list-received-items-by-type")
    public ApiResponse listReceivedItems(@RequestParam("id") Long id,
                                         @RequestParam("objectType") ObjectType objectType, Pageable pageable) {
        try {
            Page<DataCompletionItem> items = dataCompletionRateService.getListReceivedCompletionOrderItem(id, objectType, pageable);
            return ApiResponse.success(CommonMessage.OK, items);
        } catch (Exception e) {
            return ApiResponse.error(CommonMessage.SOMETHING_WENT_WRONG);
        }
    }

    @GetMapping("/list-created-orders")
    public ApiResponse listCreatedOrders(Pageable pageable) {
        try {
            Page<DataCompletionOrderLite> list = dataCompletionRateService.getCreatedCompletionOrders(pageable);
            return ApiResponse.success(CommonMessage.OK, list);
        } catch (Exception e) {
            return ApiResponse.error(CommonMessage.SOMETHING_WENT_WRONG);
        }
    }

    @PutMapping("/create-order")
    public ApiResponse createOrder(@RequestBody DataCompletionOrderPayload payload) {
        return dataCompletionRateService.save(payload);
    }

    @PostMapping("/update-order")
    public ApiResponse updateOrder(@RequestBody DataCompletionOrderPayload payload) {
        return dataCompletionRateService.update(payload);
    }
}
