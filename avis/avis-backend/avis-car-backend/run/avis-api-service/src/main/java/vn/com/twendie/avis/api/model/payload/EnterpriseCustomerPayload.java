package vn.com.twendie.avis.api.model.payload;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class EnterpriseCustomerPayload {

    @JsonProperty("customer")
    @NotNull(message = "error.blank_input")
    @Valid
    CustomerPayload customerPayload;

    @JsonProperty("admin")
    @NotNull(message = "error.blank_input")
    @Valid
    AdminCustomerPayload adminPayload;

    @JsonProperty("users")
    List<@Valid MemberCustomerPayload> userPayloads;
}
