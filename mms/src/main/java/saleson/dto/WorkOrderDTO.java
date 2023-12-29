package saleson.dto;

import com.emoldino.api.asset.resource.base.maintenance.enumeration.PM_STRATEGY;
import com.emoldino.api.common.resource.base.accesscontrol.annotation.DataLeakDetector;
import com.emoldino.framework.util.DateUtils2;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.querydsl.core.annotations.QueryProjection;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import saleson.common.enumeration.*;
import saleson.common.util.StringUtils;
import saleson.model.*;
import saleson.model.checklist.Checklist;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
public class WorkOrderDTO {
    private Long id;
    private String workOrderId;
    private WorkOrderType orderType;
    private PriorityType priority;
    private WorkOrderStatus status;
    @DataLeakDetector(disabled = true)
    private User createdBy;
    @DataLeakDetector(disabled = true)
    private List<WorkOrderUser> workOrderUsers = new ArrayList<>();
    private Instant start;
    private Instant end;

    private Instant failureTime;

    @DataLeakDetector(disabled = true)
    private Checklist checklist;

    private Boolean cost;

    private Boolean pickingList;

    @DataLeakDetector(disabled = true)
    private Checklist picklist;

    @DataLeakDetector(disabled = true)
    private List<WorkOrderAsset> workOrderAssets = new ArrayList<>();

    private List<String> attachments = new ArrayList<>();

    @DataLeakDetector(disabled = true)
    private List<Location> plantList = new ArrayList<>();

    private List<WorkOrderCost> workOrderCostList = new ArrayList<>();

    private Long picklistId;

    private String picklistItemStr;

    private Long checklistId;

    private String checklistItemStr;

    private String note;

    private Instant startedOn;
    private Instant completedOn;

    private Frequent frequent;
    private DayShiftType repeatOn;
    private Integer endAfter;

    private String details;
    private Integer numberOfBackups;

    private Boolean requestApproval;

    private Long estimatedExtendedLifeShot;
    private Long forecastedMaxShot;
    private Boolean requestApprovalEnabled;
    @DataLeakDetector(disabled = true)
    private UserLiteDTO reviewedBy;
    private String rejectedReason;

    private String rejectedChangeReason;

    private String declinedReason;

    private String cancelledReason;
    private List<Long> removeFileId;

    @DataLeakDetector(disabled = true)
    private User rejectedChangeBy;

    @DataLeakDetector(disabled = true)
    private User declinedBy;

    @DataLeakDetector(disabled = true)
    private User cancelledBy;

    @JsonIgnore
    @DataLeakDetector(disabled = true)
    private MoldMaintenance moldMaintenance;

    private Integer reportFailureShot;
    private Integer startWorkOrderShot;

    private boolean inProcessing;
    private Integer lastStep;

    private String refCode;

    private Integer preventCycle; // tooling maintenance interval
    private Integer untilNextPm;
    private Integer pmCheckpoint;
    private Integer pmCheckpointPrediction;
    private PM_STRATEGY pmStrategy;


    public List<String> getChecklistItems() {
        List<String> checklistItems = new ArrayList<>();
        if (!StringUtils.isEmpty(checklistItemStr)) {
            String[] itemValues = checklistItemStr.split("\n");
            checklistItems = Arrays.stream(itemValues).collect(Collectors.toList());
        }
        return checklistItems;
    }

    public void setChecklistItems(List<String> checklistItems) {
        this.checklistItemStr = CollectionUtils.isNotEmpty(checklistItems)
                ? String.join("\n", checklistItems) : null;
    }


    public List<String> getPicklistItems() {
        List<String> picklistItems = new ArrayList<>();
        if (!StringUtils.isEmpty(picklistItemStr)) {
            String[] itemValues = picklistItemStr.split("\n");
            picklistItems = Arrays.stream(itemValues).collect(Collectors.toList());
        }
        return picklistItems;
    }

    public void setPicklistItems(List<String> picklistItems) {
        this.picklistItemStr = CollectionUtils.isNotEmpty(picklistItems)
                ? String.join("\n", picklistItems) : null;
    }

//    public Integer getPmCheckpointPrediction() {
//        if (moldMaintenance!=null){
//            if (end.isBefore(DateUtils2.newInstant()))
//                return Math.toIntExact(Duration.between(end, DateUtils2.newInstant()).toDays())+1;
//            else return Math.toIntExact(Duration.between(DateUtils2.newInstant(), end).toDays())+1;
//        }
//        return moldMaintenance != null ? moldMaintenance.getPmCheckpointPrediction() : null;
//    }

    public Integer getAccumulatedShot() {
        return moldMaintenance != null ? moldMaintenance.getAccumulatedShot() : null;
    }

    public MaintenanceStatus getMaintenanceStatus() {
        if (getLastShot() != null && getPmCheckpoint() != null) {
            if (getLastShot() >= getPmCheckpoint()) return MaintenanceStatus.OVERDUE;
            else {
                return MaintenanceStatus.UPCOMING;
            }
        } else {
            return null;
        }
    }

    @QueryProjection
    public WorkOrderDTO(WorkOrder workOrder, User createdBy) {
        this.id = workOrder.getId();
        this.workOrderId = workOrder.getWorkOrderId();
        this.orderType = workOrder.getOrderType();
        this.priority = workOrder.getPriority();
        this.status = workOrder.getStatus();
        this.createdBy = createdBy;
        this.workOrderUsers = workOrder.getWorkOrderUsers();
        this.start = workOrder.getStart();
        this.end = workOrder.getEnd();
        this.failureTime = workOrder.getFailureTime();
        this.checklist = workOrder.getChecklist();
        this.checklistId = workOrder.getChecklistId();
        this.checklistItemStr = workOrder.getChecklistItemStr();
        this.cost = workOrder.isCost();
        this.pickingList = workOrder.isPickingList();
        this.picklistId = workOrder.getPicklistId();
        this.workOrderAssets = workOrder.getWorkOrderAssets();
        this.note = workOrder.getNote();
        this.frequent = workOrder.getFrequent();
        this.repeatOn = workOrder.getRepeatOn();
        this.endAfter = workOrder.getEndAfter();
        this.picklistItemStr = workOrder.getPicklistItemStr();
        this.picklist = workOrder.getPicklist();
        this.completedOn = workOrder.getCompletedOn();
        this.startedOn = workOrder.getStartedOn();
        this.details = workOrder.getDetails();
        this.numberOfBackups = workOrder.getNumberOfBackups();
        this.requestApprovalEnabled = workOrder.getRequestApprovalEnabled();
        this.requestApproval = workOrder.getRequestApproval();
        this.rejectedReason = workOrder.getRejectedReason();
        this.estimatedExtendedLifeShot = workOrder.getEstimatedExtendedLifeShot();
        this.rejectedChangeReason = workOrder.getRejectedChangeReason();
        this.declinedReason = workOrder.getDeclinedReason();
        this.cancelledReason = workOrder.getCancelledReason();
        this.rejectedChangeBy = workOrder.getRejectedChangeBy();
        this.declinedBy = workOrder.getDeclinedBy();
        this.cancelledBy = workOrder.getCancelledBy();
        this.moldMaintenance = workOrder.getMoldMaintenance();
        this.reportFailureShot = workOrder.getReportFailureShot();
        this.startWorkOrderShot = workOrder.getStartWorkOrderShot();
        this.refCode = workOrder.getRefCode();
    }

    public WorkOrderStatus getWorkOrderStatus() {
        if (status != WorkOrderStatus.DECLINED
                && status != WorkOrderStatus.CANCELLED
                && status != WorkOrderStatus.COMPLETED
                && status != WorkOrderStatus.REQUESTED_NOT_FINISHED
                && status != WorkOrderStatus.APPROVAL_REQUESTED
                && status != WorkOrderStatus.PENDING_APPROVAL
                && status != WorkOrderStatus.CHANGE_REQUESTED) {

            if (WorkOrderType.PREVENTATIVE_MAINTENANCE.equals(orderType)
                    && (PM_STRATEGY.SHOT_BASED.equals(pmStrategy) || pmStrategy == null)
                    && getLastShot() != null
                    && getLastShot() != 0) {
                if (getLastShot() != null && getPmCheckpoint() != null) {
                    if (getLastShot() >= getPmCheckpoint())
                        return WorkOrderStatus.OVERDUE;
                    else {
                        if (status == WorkOrderStatus.ACCEPTED) {
                            return WorkOrderStatus.UPCOMING;
                        } else {
                            return status;
                        }
                    }
                }
            }

            if (getEnd() != null && Instant.now().isAfter(getEnd())) {
                return WorkOrderStatus.OVERDUE;
            }

        }

        if (status == WorkOrderStatus.ACCEPTED) {
            return WorkOrderStatus.UPCOMING;
        }
        if (status == null) return null;
        return status;
    }

    public void bidingData(WorkOrder workOrder) {
        workOrder.setStartedOn(startedOn);
        workOrder.setCompletedOn(completedOn);
        workOrder.setNote(note);
        workOrder.setChecklistId(getChecklistId());
        workOrder.setPicklistId(getPicklistId());
        if (getChecklistId() != null) {
            Checklist c = new Checklist();
            c.setId(getChecklistId());
            workOrder.setChecklist(c);
        }
        if (getPicklistId() != null) {
            Checklist c = new Checklist();
            c.setId(getPicklistId());
            workOrder.setPicklist(c);
        }
        workOrder.setChecklistItems(getChecklistItems());
        workOrder.setPicklistItems(getPicklistItems());
        workOrder.setWorkOrderAssets(getWorkOrderAssets());
        workOrder.setFrequent(getFrequent());
        workOrder.setRepeatOn(getRepeatOn());
        workOrder.setEndAfter(getEndAfter());
        workOrder.setEstimatedExtendedLifeShot(getEstimatedExtendedLifeShot());
        workOrder.setRefCode(refCode);
    }

    public Integer getLastShot() {
        return moldMaintenance == null ? null : (moldMaintenance.getMold() == null ? null : moldMaintenance.getMold().getLastShot());
    }

    public Long getForecastedMaxShot() {
        return forecastedMaxShot;

    }

    public Integer getShotUntilNextPM() {
        return moldMaintenance == null ? null : (moldMaintenance.getShotUntilNextPM() == null ? null : moldMaintenance.getShotUntilNextPM());
    }

    public MoldMaintenanceDTO getMoldMaintenanceData() {
        return new MoldMaintenanceDTO(this.moldMaintenance);
    }

//    public Instant getEnd() {
//        if (WorkOrderType.PREVENTATIVE_MAINTENANCE.equals(orderType) && (PM_STRATEGY.SHOT_BASED.equals(pmStrategy) || pmStrategy == null)) {
//            if (getLastShot() != null && getPmCheckpoint() != null && getLastShot() >= getPmCheckpoint()) return end;
//            else if (getPmCheckpointPrediction() != null)
//                return Instant.now().plusSeconds(getPmCheckpointPrediction() * 24 * 60 * 60);
//            else return end;
//        } else return end;
//    }
}
