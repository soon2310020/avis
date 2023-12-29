package saleson.model.data;

import com.querydsl.core.annotations.QueryProjection;
import lombok.Data;
import saleson.model.MoldCycleTime;
import saleson.model.MoldMaintenance;

@Data
public class MoldMaintenancePartExtraData {
    private MoldMaintenance moldMaintenance;
    private String partCode;

    @QueryProjection
    public MoldMaintenancePartExtraData(MoldMaintenance moldMaintenance, String partCode){
        this.moldMaintenance = moldMaintenance;
        this.partCode = partCode;
    }
}
