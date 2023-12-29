package saleson.dto;

import com.emoldino.framework.util.ValueUtils;
import lombok.Data;
import lombok.NoArgsConstructor;
import saleson.common.enumeration.MaintenanceStatus;
import saleson.model.MoldMaintenance;

import java.time.Instant;

@Data
@NoArgsConstructor
public class MoldMaintenanceDTO {
    private MaintenanceStatus maintenanceStatus;


    private Integer lastShotMade; // last shot made since the last period


    private Integer preventCycle;
    private Integer preventUpcoming;
    private Integer periodStart;
    private Integer periodEnd;
    private Integer shotCount;


    private Instant startTime;
    private Instant endTime;

    private Instant overdueTime;

    private Integer dueDate;

    public Integer pmCheckpointPrediction;
    public Integer shotUntilNextPM;

    private Integer period;


    public MoldMaintenanceDTO(MoldMaintenance moldMaintenance ) {
        if(moldMaintenance == null )return;
        ValueUtils.map(moldMaintenance,this);
        if (moldMaintenance.getPeriodStart() != null && moldMaintenance.getMold().getPreventUpcoming() != null)
            this.period=(moldMaintenance.getPeriodStart() + moldMaintenance.getMold().getPreventUpcoming());
        else if (moldMaintenance.getPeriodEnd() != null && moldMaintenance.getMold().getPreventOverdue() != null)
            this.period=(moldMaintenance.getPeriodEnd() - moldMaintenance.getMold().getPreventOverdue());
    }
}
