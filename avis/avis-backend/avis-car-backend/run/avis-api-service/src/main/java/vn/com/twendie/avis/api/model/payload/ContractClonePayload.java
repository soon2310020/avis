package vn.com.twendie.avis.api.model.payload;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ContractClonePayload {

    @JsonProperty("code_prefix")
    private String codePrefix;

    @JsonProperty("contract_type")
    private String contractType;
}
