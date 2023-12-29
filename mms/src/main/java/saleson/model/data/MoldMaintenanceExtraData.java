package saleson.model.data;

import com.querydsl.core.annotations.QueryProjection;
import lombok.Data;
import saleson.model.Mold;
import saleson.model.MoldMaintenance;

@Data
public class MoldMaintenanceExtraData {
    private MoldMaintenance moldMaintenance;
    private Integer period;
    private Double executionRate;
    private Mold mold;

    @QueryProjection
    public MoldMaintenanceExtraData(MoldMaintenance moldMaintenance, Integer period){
        this.moldMaintenance = moldMaintenance;
        this.period = period;
    }

    @QueryProjection
    public MoldMaintenanceExtraData(MoldMaintenance moldMaintenance, Double executionRate){
        this.moldMaintenance = moldMaintenance;
        this.executionRate = executionRate;
    }

    @QueryProjection
    public MoldMaintenanceExtraData(MoldMaintenance moldMaintenance){
        this.moldMaintenance = moldMaintenance;
    }

    public MoldMaintenanceExtraData(MoldMaintenance moldMaintenance, Mold mold) {
        this.moldMaintenance = moldMaintenance;
        this.mold = mold;
    }
}
