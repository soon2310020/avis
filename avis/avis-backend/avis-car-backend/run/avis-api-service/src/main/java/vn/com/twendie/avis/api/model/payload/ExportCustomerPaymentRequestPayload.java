package vn.com.twendie.avis.api.model.payload;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.sql.Timestamp;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ExportCustomerPaymentRequestPayload {

    @NotNull(message = "error.blank_input")
    private Timestamp from;

    @NotNull(message = "error.blank_input")
    private Timestamp to;

    @JsonProperty("contract_id")
    @NotNull(message = "error.blank_input")
    private Long contractId;

    @JsonProperty("member_customer_ids")
    private String memberCustomerIds;
}
