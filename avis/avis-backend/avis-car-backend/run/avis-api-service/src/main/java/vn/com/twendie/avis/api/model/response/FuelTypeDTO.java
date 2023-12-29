package vn.com.twendie.avis.api.model.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.io.Serializable;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class FuelTypeDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @JsonProperty("id")
    private Long id;

    @JsonProperty("code")
    private String code;

    @JsonProperty("name")
    private String name;

    @JsonProperty("fuel_type_group_id")
    private Long fuelTypeGroupId;

    @JsonIgnore
    private FuelTypeGroupDTO fuelTypeGroup;

    public Long getFuelTypeGroupId() {
        return fuelTypeGroup != null ? fuelTypeGroup.getId() : fuelTypeGroupId;
    }

}
