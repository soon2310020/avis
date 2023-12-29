package vn.com.twendie.avis.api.model.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class CreateContractDriverDTO {

    @JsonProperty("id") private Long id;
    @JsonProperty("email") private String email;
    @JsonProperty("id_card") private String idCard;
    @JsonProperty("mobile") private String mobile;
    @JsonProperty("country_code") private String countryCode;
    @JsonProperty("name") private String name;
    @JsonProperty("status") private Integer status;
    @JsonProperty("in_journey") private boolean inJourney;

    @JsonProperty("contract_contained") ContractContainedDTO contract;
}
