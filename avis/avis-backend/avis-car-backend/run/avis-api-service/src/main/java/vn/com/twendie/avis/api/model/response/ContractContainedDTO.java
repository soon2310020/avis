package vn.com.twendie.avis.api.model.response;

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
public class ContractContainedDTO {

    @JsonProperty("id") private Long id;
    @JsonProperty("code") private String code;
    @JsonProperty("driver_id") private Long driverId;
    @JsonProperty("vehicle_id") private Long vehicleId;
    @JsonProperty("contract_type") private String contractType;
    @JsonProperty("is_lend") private boolean isLend;
}
