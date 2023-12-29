package saleson.api.workOrder;

import com.emoldino.api.common.resource.base.accesscontrol.annotation.DataLeakDetector;
import com.emoldino.framework.util.BeanUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import saleson.api.configuration.ColumnTableConfigService;
import saleson.api.mold.MoldService;
import saleson.api.mold.payload.ExportPayload;
import saleson.api.mold.payload.MoldPayload;
import saleson.api.workOrder.payload.ApproveRequestApprovalPayload;
import saleson.api.workOrder.payload.ExportWorkOrderPayload;
import saleson.api.workOrder.payload.SearchPayload;
import saleson.api.workOrder.payload.WorkOrderPayload;
import saleson.common.constant.CommonMessage;
import saleson.common.domain.MultipartFormData;
import saleson.common.enumeration.*;
import saleson.common.payload.ApiResponse;
import saleson.common.util.DateUtils;
import saleson.common.util.StringUtils;
import saleson.dto.UpdateWorkOrderDTO;
import saleson.dto.WorkOrderDTO;
import saleson.dto.WorkOrderResponse;
import saleson.model.WorkOrder;
import saleson.model.data.MaintenanceData;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Instant;
import java.util.Calendar;

@RestController
@RequestMapping("/api/work-order")
@Slf4j
public class WorkOrderController {

    @Autowired
    private WorkOrderService workOrderService;
    @Autowired
    private ObjectMapper objectMapper;

    @PostMapping("add-multipart")
    public ResponseEntity<?> newWorkOderMultipart(@RequestParam(required = false, name = "draft") Boolean draft, MultipartFormData formData) {
        try {
            WorkOrderPayload payload = objectMapper.readValue(formData.getPayload(), WorkOrderPayload.class);
            payload.setFiles(formData.getThirdFiles());
            WorkOrder workOrder = payload.getModel();
            workOrder.setStatus(WorkOrderStatus.REQUESTED);

            WorkOrder result = workOrderService.saveNormalWorkOrder(workOrder, payload);

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            log.error("Error when create new Work Order", e);
            return ResponseEntity.badRequest().body("Fail!");
        }
    }

    @PostMapping("request-approval")
    public ResponseEntity<?> requestApproval(MultipartFormData formData) {
        try {
            WorkOrderPayload payload = objectMapper.readValue(formData.getPayload(), WorkOrderPayload.class);
            payload.setCostFile(formData.getSecondFiles());
            payload.setFiles(formData.getThirdFiles());
            WorkOrder workOrder = payload.getModel();
            workOrder.setStatus(WorkOrderStatus.APPROVAL_REQUESTED);
            workOrder.setCmRequested(WorkOrderType.CORRECTIVE_MAINTENANCE == payload.getOrderType());
            //todo check valid data

            WorkOrder result = workOrderService.saveRequestApprovalWorkOrder(workOrder, payload);
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            log.error("Error when create new Work Order", e);
            return ResponseEntity.badRequest().body("Fail!");
        }
    }

    @PostMapping("approve-request-approval/{id}")
    public ResponseEntity<?> approveRequestApproval(@PathVariable("id") Long id,
                                                    @RequestParam("approved") boolean approved,
                                                    @RequestBody ApproveRequestApprovalPayload payload) {

        try {
            WorkOrder result = workOrderService.approveRequestApproval(id, approved, payload);
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            log.error("Error when approve CM request", e);
            return ResponseEntity.badRequest().body("Fail!");
        }
    }

    @PostMapping("update-my-order/{id}")
    public ResponseEntity<?> updateMyOrder(@PathVariable("id") Long id, @RequestBody UpdateWorkOrderDTO updateWorkOrderDTO) {

        try {
            WorkOrder result = workOrderService.updateMyOrder(id, updateWorkOrderDTO);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Error when update Work Order", e);
            return ResponseEntity.badRequest().body("Fail!");
        }
    }

    @DataLeakDetector(disabled = true)
    @GetMapping
    public ResponseEntity<WorkOrderResponse> listWorkOrder(SearchPayload searchPayload, Pageable pageable) {
        return ResponseEntity.ok(workOrderService.searchWorkOrder(searchPayload, pageable));
    }

    @GetMapping("generate-id")
    public String generateId() {
        return workOrderService.generateWorkOrderId();
    }

    @GetMapping("{id}")
    public ApiResponse getWorkOrderDetail(@PathVariable(value = "id") Long id) {
        return workOrderService.getWorkOrderDetail(id);
    }

    @PostMapping("/complete")
    public ApiResponse completeWorkOrder(MultipartFormData formData) throws IOException {
        WorkOrderDTO workOrderDTO = objectMapper.readValue(formData.getPayload(), WorkOrderDTO.class);
        ApiResponse response = workOrderService.completeWorkOrder(workOrderDTO, formData.getSecondFiles(), formData.getFiles(), formData.getForthFiles());

        return response;
    }

    @PostMapping("/request-change")
    public ApiResponse requestChangeWorkOrder(MultipartFormData formData) throws IOException {
        return workOrderService.requestChange(formData);
    }

    @PostMapping("/approve-change")
    public ApiResponse approveRequestChange(@RequestParam("id") Long id,
                                            @RequestBody UpdateWorkOrderDTO updateWorkOrderDTO) {
        return workOrderService.approveRequestChange(id, updateWorkOrderDTO);
    }

    @PostMapping("/review-complete-request-approval")
    public ApiResponse reviewCompletionRequestApproval(@RequestBody WorkOrderPayload payload) {
        return workOrderService.reviewCompletionRequestApproval(payload);
    }

    @PostMapping("/re-open")
    public ResponseEntity<?> reopenWorkOder(MultipartFormData formData) {
        try {
            WorkOrderPayload payload = objectMapper.readValue(formData.getPayload(), WorkOrderPayload.class);
            payload.setFiles(formData.getThirdFiles());
            WorkOrder workOrder = payload.getModel(workOrderService.findById(payload.getId()));
            workOrder.setStatus(WorkOrderStatus.REQUESTED);
            workOrder.setCompletedOn(null);
            WorkOrder result = workOrderService.saveNormalWorkOrder(workOrder, payload);
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            log.error("Error when re-open Work Order", e);
            return ResponseEntity.badRequest().body("Fail!");
        }
    }

    @PostMapping("/edit-multipart")
    public ResponseEntity<?> editWorkOderMultipart(MultipartFormData formData) {
        try {
            WorkOrderPayload payload = objectMapper.readValue(formData.getPayload(), WorkOrderPayload.class);
            payload.setFiles(formData.getThirdFiles());
            payload.setPowFiles(formData.getForthFiles());
            WorkOrder workOrder = payload.getModel(workOrderService.findById(payload.getId()));

            WorkOrder result;
            if (payload.getOrderType().equals(WorkOrderType.CORRECTIVE_MAINTENANCE)) {
                result = workOrderService.updateCMWorkOrder(workOrder, payload);
            } else {
                result = workOrderService.updateNormalWorkOrder(workOrder, payload);
            }
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            log.error("Error when re-open Work Order", e);
            return ResponseEntity.badRequest().body("Fail!");
        }
    }

    @GetMapping("/export")
    public ResponseEntity<?> exportExcelDynamicOneToolingPerFile(HttpServletResponse response, ExportWorkOrderPayload payload) {
        try {
            workOrderService.exportWorkOrder(response, payload);
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(e.getMessage());
        }
        return ResponseEntity.ok("Success.");
    }



    @GetMapping(value = "/maintenance/exist")
    public ResponseEntity existMaintenanceWorkOrder(@RequestParam(value = "moldId") Long moldId) {
        return ResponseEntity.ok(workOrderService.existMaintenanceWorkOrder(moldId));
    }

    @GetMapping(value = "/request-change-work-order")
    public ApiResponse getWorkOrderRequestChange(@RequestParam(value = "originId") Long originId) {
        return workOrderService.getRequestChangeWorkOrder(originId);
    }
}
