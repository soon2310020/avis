package vn.com.twendie.avis.tracking.model.wrapper;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import vn.com.twendie.avis.tracking.model.tracking.VehicleActivity;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class VehicleActivityWrapper {

    @JsonProperty("ActivityDetails")
    private List<VehicleActivity> vehicleActivities;

    @JsonProperty("ErrorType")
    private int errorType;

    @JsonProperty("MessageResult")
    private String messageResult;

}
