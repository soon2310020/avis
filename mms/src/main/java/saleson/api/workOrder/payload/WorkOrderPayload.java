package saleson.api.workOrder.payload;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;
import saleson.common.enumeration.*;
import saleson.model.WorkOrder;
import saleson.model.checklist.Checklist;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Data
public class WorkOrderPayload {
    private Long id;

    private String workOrderId;
    private WorkOrderType orderType;
    private PriorityType priority;

    private List<Long> moldIds = new ArrayList<>();

    private List<Long> machineIds = new ArrayList<>();

    private List<Long> terminalIds = new ArrayList<>();

    private List<Long> counterIds = new ArrayList<>();

    private List<Long> userIds = new ArrayList<>();

    private String details;

    private Long checklistId;

    private boolean cost;
    private boolean pickingList;

    private Instant start;
    private Instant end;

    private Frequent frequent;
    private DayShiftType repeatOn;
    private Integer endAfter;

    private MultipartFile[] files;
    private MultipartFile[] powFiles;

    //for CM
    private Instant failureTime;
    private Integer numberOfBackups;
    private Double costEstimate;
    private String costDetails;
    private MultipartFile[] costFile;

    private Long reportedBy;
    private ReviewWorkOrderAction action;
    private String rejectedReason;
    private String refCode;

    private boolean created = false;
    private Integer reportFailureShot;

    public WorkOrder getModel() {
        WorkOrder workOrder = new WorkOrder();
        bindData(workOrder);
        return workOrder;
    }

    public WorkOrder getModel(WorkOrder workOrder) {
        bindData(workOrder);
        return workOrder;
    }

    public void bindData(WorkOrder workOrder) {
        workOrder.setWorkOrderId(workOrderId);
        workOrder.setOrderType(orderType);
        workOrder.setPriority(priority);
        workOrder.setDetails(details);
        workOrder.setCost(cost);
        workOrder.setPickingList(pickingList);
        workOrder.setStart(start);
        workOrder.setEnd(end);
        workOrder.setFrequent(frequent);
        workOrder.setRepeatOn(repeatOn);
        workOrder.setEndAfter(endAfter);
        workOrder.setFailureTime(failureTime);
        workOrder.setNumberOfBackups(numberOfBackups);
        workOrder.setCostEstimate(costEstimate);
        workOrder.setCostDetails(costDetails);
        workOrder.setRequestApprovalEnabled(true);
        workOrder.setRefCode(refCode);
        workOrder.setReportFailureShot(reportFailureShot);

        if (checklistId != null) {
            Checklist checklist = new Checklist();
            checklist.setId(checklistId);
            workOrder.setChecklist(checklist);
        }
    }
}
