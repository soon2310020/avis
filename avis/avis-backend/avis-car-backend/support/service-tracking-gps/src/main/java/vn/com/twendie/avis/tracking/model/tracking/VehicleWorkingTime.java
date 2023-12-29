package vn.com.twendie.avis.tracking.model.tracking;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Time;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VehicleWorkingTime {

    @JsonProperty("from")
    private Time from;

    @JsonProperty("to")
    private Time to;

}
