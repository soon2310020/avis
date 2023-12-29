package vn.com.twendie.avis.api.model.filter;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.sql.Timestamp;
import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ContractFilter {

    @JsonProperty("contract_type")
    private ContractTypeFilter contractType;

    @JsonProperty("code")
    private String code;

    @JsonProperty("prefix_code_in")
    private List<String> prefixCodeIn;

    @JsonProperty("sign_date")
    private Timestamp signDate;

    @JsonProperty("sign_date_gte")
    private Timestamp signDateGte;

    @JsonProperty("sign_date_lte")
    private Timestamp signDateLte;

    @JsonProperty("term")
    private Integer term;

    @JsonProperty("branch")
    private BranchFilter branch;

    @JsonProperty("customer")
    private ContractCustomerFilter customer;

    @JsonProperty("member_customer")
    private ContractMemberCustomerFilter memberCustomer;

    @JsonProperty("status")
    private Integer status;

    @JsonProperty("status_not")
    private Integer statusNot;

    @JsonProperty("driver_is_transferred_another")
    private Boolean driverIsTransferredAnother;

    @JsonProperty("vehicle_is_transferred_another")
    private Boolean vehicleIsTransferredAnother;

    @JsonProperty("from_datetime_gte")
    private Timestamp fromDatetimeGte;

    @JsonProperty("from_datetime_lte")
    private Timestamp fromDatetimeLte;

    @JsonProperty("vehicle")
    private ContractVehicleFilter vehicle;

    @JsonProperty("driver")
    private ContractDriverFilter driver;

    @JsonProperty("created_by")
    private CreatedByFilter createdBy;

}
