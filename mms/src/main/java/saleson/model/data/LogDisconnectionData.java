package saleson.model.data;

import com.querydsl.core.annotations.QueryProjection;
import lombok.Data;
import saleson.common.enumeration.EquipmentType;
import saleson.common.enumeration.Event;

import java.time.Instant;

@Data
public class LogDisconnectionData {
    Instant createdAt;
    Long equipmentId;
    EquipmentType equipmentType;
    Event event;

    @QueryProjection
    public LogDisconnectionData(Instant createdAt, Long equipmentId, Event event){
        this.createdAt = createdAt;
        this.equipmentId = equipmentId;
        this.event = event;
    }
}
