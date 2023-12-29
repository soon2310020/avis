package vn.com.twendie.avis.api.model.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class VehicleDTO {

    @JsonProperty("id") private Long id;
    @JsonProperty("model") private String model;
    @JsonProperty("number_plate") private String numberPlate;
    @JsonProperty("color" )private String color;
    @JsonProperty("type") private String type;
    @JsonProperty("number_seat") private Integer numberSeat;
    @JsonProperty("status") private Integer status;
    @JsonProperty("in_journey") private boolean inJourney;

    @JsonProperty("contract_contained") ContractContainedDTO contract;
    @JsonProperty("fuel_type_group_id") Long fuelTypeGroupId;
    @JsonIgnore FuelTypeGroupDTO fuelTypeGroup;

    public Long getFuelTypeGroupId() {
        return fuelTypeGroup != null ? fuelTypeGroup.getId() : fuelTypeGroupId;
    }


}
