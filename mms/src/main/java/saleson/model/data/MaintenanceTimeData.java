package saleson.model.data;

import com.querydsl.core.annotations.QueryProjection;
import lombok.Data;

import java.time.Instant;

@Data
public class MaintenanceTimeData {
    Instant startTime;
    Instant endTime;

    @QueryProjection
    public MaintenanceTimeData(Instant startTime, Instant endTime){
        this.startTime = startTime;
        this.endTime = endTime;
    }
}
