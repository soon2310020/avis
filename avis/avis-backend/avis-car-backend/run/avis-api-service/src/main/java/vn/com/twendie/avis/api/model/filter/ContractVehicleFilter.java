package vn.com.twendie.avis.api.model.filter;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ContractVehicleFilter {

    @JsonProperty("id_in")
    private List<Integer> idIn;

    @JsonProperty("type")
    private String type;

    @JsonProperty("number_plate")
    private String numberPlate;

    @JsonProperty("branch")
    private BranchFilter branch;

    @JsonProperty("operation_admin")
    private UserFilter operationAdmin;

    @JsonProperty("accountant")
    private UserFilter accountant;

}
