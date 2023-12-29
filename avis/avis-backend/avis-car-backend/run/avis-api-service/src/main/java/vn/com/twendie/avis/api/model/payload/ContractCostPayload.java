package vn.com.twendie.avis.api.model.payload;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ContractCostPayload {

    @JsonProperty("code")
    private String code;

    @JsonProperty("value")
    private int value;

}
