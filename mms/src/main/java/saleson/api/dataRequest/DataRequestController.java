package saleson.api.dataRequest;

import com.emoldino.api.common.resource.base.accesscontrol.annotation.DataLeakDetector;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import saleson.api.dataRequest.payload.DataRequestDetail;
import saleson.api.dataRequest.payload.DataRequestPayload;
import saleson.api.dataRequest.payload.UpdateStatusDTO;
import saleson.common.domain.MultipartFormData;
import saleson.common.enumeration.DataCompletionRequestStatus;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/api/data-request")
public class DataRequestController {

    @Autowired
    private DataRequestService dataRequestService;

    @Autowired
    private ObjectMapper objectMapper;


    @DataLeakDetector(disabled = true)
    @GetMapping
    public ResponseEntity getDataRequest(DataRequestPayload payload, Pageable pageable) {
        return ResponseEntity.ok(dataRequestService.getDataRequest(payload, pageable));
    }

    @DataLeakDetector(disabled = true)
    @GetMapping(value = "/{id}")
    public ResponseEntity getDetailDataRequest(@PathVariable(value = "id") Long id) throws ExecutionException, InterruptedException {
        return ResponseEntity.ok(dataRequestService.getDetailDataRequest(id));
    }

    @GetMapping(value = "/completion-rate-widget-data")
    public ResponseEntity getCompletionRateWidgetData() {
        return ResponseEntity.ok(dataRequestService.getCompletionRateWidgetData());
    }

    @PostMapping
    public ResponseEntity saveDataRequest(MultipartFormData formData) throws IOException {
        DataRequestDetail dataRequestDetail = objectMapper.readValue(formData.getPayload(), DataRequestDetail.class);
        dataRequestDetail.setFiles(formData.getFiles());
        return ResponseEntity.ok(dataRequestService.saveDataRequest(dataRequestDetail));
    }

    @PostMapping(value = "/changeStatus")
    public ResponseEntity changeStatusDataRequest(@RequestBody UpdateStatusDTO updateStatusDTO) {
        dataRequestService.changeStatusDataRequest(updateStatusDTO);
        return ResponseEntity.ok().build();
    }

    @GetMapping(value = "/get-data-request-id")
    public ResponseEntity getDataRequestId() {
        return ResponseEntity.ok(dataRequestService.getDataRequestId());
    }

    @GetMapping(value = "/get-number-incomplete-data")
    public ResponseEntity getNumberIncompleteData() throws ExecutionException, InterruptedException {
        return ResponseEntity.ok(dataRequestService.getNumberIncompleteData(null));
    }

    @PostMapping("/re-open")
    public ResponseEntity reOpenDataRequest(MultipartFormData formData) throws IOException {
        DataRequestDetail dataRequestDetail = objectMapper.readValue(formData.getPayload(), DataRequestDetail.class);
        dataRequestDetail.setDataRequestStatus(DataCompletionRequestStatus.REQUESTED);
        dataRequestDetail.setReOpen(true);
        return ResponseEntity.ok(dataRequestService.saveDataRequest(dataRequestDetail));
    }


}
