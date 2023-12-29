package vn.com.twendie.avis.api.model.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ForgotPasswordResponse {

    @JsonProperty(value = "email")
    private String email;

    @JsonProperty(value = "country_code")
    private String countryCode;

    @JsonProperty(value = "mobile")
    private String mobile;

}
