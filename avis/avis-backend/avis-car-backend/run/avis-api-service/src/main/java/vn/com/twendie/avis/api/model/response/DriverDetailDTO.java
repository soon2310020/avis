package vn.com.twendie.avis.api.model.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.sql.Timestamp;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class DriverDetailDTO {

    @JsonProperty("id")
    private Long id;

    @JsonProperty("code")
    private String code;

    @JsonProperty("name")
    private String name;

    @JsonProperty("username")
    private String username;

    @JsonProperty("birthdate")
    private Timestamp birthdate;

    @JsonProperty("id_card")
    private String idCard;

    @JsonProperty("card_type")
    private String cardType;

    @JsonProperty("iso2")
    private String iso2;

    @JsonProperty("country_code")
    private String countryCode;

    @JsonProperty("mobile")
    private String mobile;

    @JsonProperty("mobile_full")
    private String mobileFull;

    @JsonProperty("email")
    private String email;

    @JsonProperty("address")
    private String address;

    @JsonProperty("department")
    private String department;

    @JsonProperty("driver_licenses")
    private String driverLicenses;

    @JsonProperty("driver_license_number")
    private String driverLicenseNumber;

    @JsonProperty("driver_license_expiry_date")
    private Timestamp driverLicenseExpiryDate;

    @JsonProperty("know_english")
    private Boolean knowEnglish;

    @JsonProperty("ddt_certificate")
    private Boolean ddtCertificate;

    @JsonProperty("active")
    private Boolean active;

    @JsonProperty("status")
    private Integer status;

    @JsonProperty("current_journey_diary_id")
    private Long currentJourneyDiaryId;

    @JsonProperty("current_contract_id")
    private Long currentContractId;

    @JsonProperty("lending_contract_id")
    private Long lendingContractId;

    @JsonProperty("in_contract")
    private Boolean inContract;

    @JsonProperty("note")
    private String note;

    @JsonProperty("user_group_id")
    private UserGroupDTO userGroup;

    @JsonProperty("branch_id")
    private BranchDTO branch;

    @JsonProperty("unit_operator_id")
    private UnitOperatorDTO unitOperator;
    
}
