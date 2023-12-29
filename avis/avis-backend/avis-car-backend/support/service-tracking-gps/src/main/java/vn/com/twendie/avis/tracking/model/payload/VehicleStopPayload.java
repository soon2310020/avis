package vn.com.twendie.avis.tracking.model.payload;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class VehicleStopPayload extends Credential {

    public static final int MIN_STOP_TIME = 5;

    @JsonProperty("vehiclePlate")
    private String vehiclePlate;

    @JsonProperty("fromDate")
    private String fromDate;

    @JsonProperty("toDate")
    private String toDate;

    @JsonProperty("totalTime")
    private int totalTime;

    @JsonProperty("minuteMachineOn")
    private int minuteMachineOn;

    @JsonProperty("minuteConditionOn")
    private int minuteConditionOn;

    @JsonProperty("stopValid")
    private int stopValid;

}
