package vn.com.twendie.avis.api.model.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class MemberCustomerDetailDTO {

    @JsonProperty("id")
    private Long id;

    @JsonProperty("code")
    private String code;

    @JsonProperty("name")
    private String name;

    @JsonProperty("department")
    private String department;

    @JsonProperty("role")
    private String role;

    @JsonProperty("email")
    private String email;

    @JsonProperty("mobile")
    private String mobile;

    @JsonProperty("country_code")
    private String countryCode;

    @JsonProperty("iso2")
    private String iso2;

    @JsonProperty("in_contract")
    private boolean inContract;

    @JsonProperty("active")
    private boolean active;

    @JsonProperty("customer")
    private CustomerDTO customer;

    @JsonProperty("children")
    private List<MemberCustomerDTO> children;

}
