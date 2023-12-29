package vn.com.twendie.avis.api.model.filter;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.sql.Timestamp;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class AdminCustomerFilter {

    @JsonProperty("customer_name")
    private String customerName;

    @JsonProperty("tax_code")
    private String taxCode;

    @JsonProperty("address")
    private String address;

    @JsonProperty("admin_name")
    private String adminName;

    @JsonProperty("created_by")
    private String createdBy;

    @JsonProperty("created_at")
    private Timestamp createdAt;

}
