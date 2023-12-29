package vn.com.twendie.avis.tracking.model.payload;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class VehicleFeeStationPayload extends Credential {

    @JsonProperty("vehiclePlate")
    private String vehiclePlate;

    @JsonProperty("fromDate")
    private String fromDate;

    @JsonProperty("toDate")
    private String toDate;

}
