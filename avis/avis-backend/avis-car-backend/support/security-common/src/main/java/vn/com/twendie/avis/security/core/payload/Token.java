package vn.com.twendie.avis.security.core.payload;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class Token {

    @JsonProperty("access_token")
    // TODO: uncomment below code to stop logging user token
    // @ToString.Exclude
    private String accessToken;

    @JsonProperty("refresh_token")
    // TODO: uncomment below code to stop logging user token
    // @ToString.Exclude
    private String refreshToken;

    @JsonProperty("expires_in")
    private Integer expiresIn;

}
