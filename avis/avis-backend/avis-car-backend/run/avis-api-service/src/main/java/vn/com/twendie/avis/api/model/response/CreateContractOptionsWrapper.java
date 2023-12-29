package vn.com.twendie.avis.api.model.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class CreateContractOptionsWrapper implements Serializable {

    private static final long serialVersionUID = 1L;

    @JsonProperty("branches")
    private List<BranchDTO> branches;

    @JsonProperty("fuel_types")
    private List<FuelTypeDTO> fuelTypes;

    @JsonProperty("rental_service_types")
    private List<RentalServiceTypeDTO> rentalServiceTypes;

    @JsonProperty("working_day_types")
    private List<WorkingDayDTO> workingDayTypes;

    @JsonProperty("over_time_policies")
    private OverTimePolicyWrapper policyWrapper;

}
