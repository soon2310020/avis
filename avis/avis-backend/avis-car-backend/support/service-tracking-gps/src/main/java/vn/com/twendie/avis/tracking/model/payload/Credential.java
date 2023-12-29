package vn.com.twendie.avis.tracking.model.payload;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class Credential {

    @JsonProperty("CustomerCode")
    private String customerCode;

    @JsonProperty("Key")
    private String key;

    public void setCredential(Credential credential) {
        setCustomerCode(credential.getCustomerCode());
        setKey(credential.getKey());
    }

}
