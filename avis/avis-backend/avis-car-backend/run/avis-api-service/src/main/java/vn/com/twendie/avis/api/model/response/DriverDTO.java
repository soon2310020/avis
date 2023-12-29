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
public class DriverDTO {
    
    @JsonProperty("id")
    private Long id;

    @JsonProperty("code")
    private String code;

    @JsonProperty("department")
    private String department;

    @JsonProperty("email")
    private String email;

    @JsonProperty("id_card")
    private String idCard;

    @JsonProperty("mobile")
    private String mobile;

    @JsonProperty("name")
    private String name;
    
}
