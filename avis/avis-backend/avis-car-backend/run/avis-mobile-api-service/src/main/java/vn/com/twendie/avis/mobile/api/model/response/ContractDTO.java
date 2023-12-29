package vn.com.twendie.avis.mobile.api.model.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ContractDTO {

    @JsonProperty("id")
    private Long id;

    @JsonProperty("code")
    private String code;

    @JsonProperty("from_datetime")
    private Date fromDatetime;

    @JsonProperty("to_datetime")
    private Date toDatetime;

    @JsonProperty("customer_id")
    private Integer customerId;

    @JsonProperty("contract_type")
    private ContractTypeDTO contractType;

    @JsonProperty("vehicle")
    private VehicleDTO vehicle;

    @JsonProperty("customer")
    private CustomerDTO customer;

    @JsonProperty("member_customer")
    private MemberCustomerDTO memberCustomer;

    @JsonProperty("status")
    private Integer status;

    @JsonProperty("note")
    private String note;
    
}