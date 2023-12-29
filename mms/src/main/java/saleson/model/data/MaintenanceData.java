package saleson.model.data;

import lombok.Data;
import saleson.common.enumeration.MaintenanceStatus;
import saleson.dto.WorkOrderDTO;
import saleson.model.Machine;
import saleson.model.Mold;
import saleson.model.MoldMaintenance;

import java.time.Instant;

@Data
public class MaintenanceData {
    private Long id;
    private Long moldId;
    private Mold mold;
    private MaintenanceStatus maintenanceStatus;
    private String checklist;
    private Integer preventCycle;
    private Integer preventUpcoming;
    private Integer periodStart;
    private Integer periodEnd;
    private Integer period;
    private Integer shotCount;

    private Instant maintenancedAt;
    private String maintenanceBy;			// 정비 완료 이름.

    private Instant startTime;
    private Instant endTime;

    private Instant overdueTime;

    private Double executionRate;
    private Instant createdAt;
    private Instant updatedAt;
    private Integer dueDate;

    private Integer lastShotMade; // Last shot made since the latest maintenance period
    private Integer shotUntilNextPM;

    private Integer accumulatedShot;

    private WorkOrderDTO workOrder;

    private Integer pmCheckpointPrediction;

    public MaintenanceData(MoldMaintenance moldMaintenance, Double executionRate){
        this.id = moldMaintenance.getId();
        this.moldId = moldMaintenance.getMoldId();
        this.mold = moldMaintenance.getMold();
        this.maintenanceStatus = moldMaintenance.getMaintenanceStatus();
        this.checklist = moldMaintenance.getChecklist();
        this.preventCycle = moldMaintenance.getPreventCycle();
        this.preventUpcoming = moldMaintenance.getPreventUpcoming();
        this.periodStart = moldMaintenance.getPeriodStart();
        this.periodEnd = moldMaintenance.getPeriodEnd();
        this.shotCount = moldMaintenance.getShotCount();

        this.maintenancedAt = moldMaintenance.getMaintenancedAt();
        this.maintenanceBy = moldMaintenance.getMaintenanceBy();

        this.startTime = moldMaintenance.getStartTime();
        this.endTime = moldMaintenance.getEndTime();
        this.overdueTime = moldMaintenance.getOverdueTime();
        this.createdAt = moldMaintenance.getCreatedAt();
        this.updatedAt = moldMaintenance.getUpdatedAt();

        this.executionRate = executionRate;
        this.dueDate =  moldMaintenance.getDueDate();
        this.lastShotMade = moldMaintenance.getLastShotMade();
        this.shotUntilNextPM = moldMaintenance.getShotUntilNextPM();
        this.accumulatedShot = moldMaintenance.getAccumulatedShot();
        this.pmCheckpointPrediction = moldMaintenance.getPmCheckpointPrediction();
    }

    public Machine getMachine() {
        return mold != null ? mold.getMachine() : null;
    }

}
