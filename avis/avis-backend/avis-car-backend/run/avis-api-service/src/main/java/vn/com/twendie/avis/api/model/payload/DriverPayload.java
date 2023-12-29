package vn.com.twendie.avis.api.model.payload;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import javax.validation.constraints.*;
import java.sql.Timestamp;

@Data
public class DriverPayload {

    @JsonProperty("name")
    @NotBlank(message = "driver.error.driver_name_is_invalid")
    @Size(max = 255, message = "jdd.error.driver_name_too_long")
    private String name;

    @JsonProperty("birthdate")
    private Timestamp birthdate;

    @JsonProperty("id_card")
    @Size(max = 25, message = "valid_error.id_card_too_long")
    private String idCard;

    @JsonProperty("card_type")
    @Min(value = 1, message = "valid_error.card_type_wrong_format")
    @Max(value = 3, message = "valid_error.card_type_wrong_format")
    private Integer cardType;

    @JsonProperty("email")
    @Size(max = 100, message = "valid_error.email_too_long")
    @Email(regexp = "^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,4}$", flags = Pattern.Flag.CASE_INSENSITIVE, message = "valid_error.email_wrong_format")
    private String email;

    @JsonProperty("iso2")
    @NotBlank(message = "valid_error.iso2_is_blank")
    private String iso2;

    @JsonProperty("country_code")
    @NotBlank(message = "valid_error.country_code_is_blank")
    private String countryCode;

    @JsonProperty("mobile")
    @Pattern(regexp = "^\\s*\\d+\\s*$", message = "valid_error.phone_number_wrong_format")
    @NotBlank(message = "valid_error.phone_number_wrong_format")
    @Size(max = 15, message = "valid_error.phone_number_too_long")
    private String mobile;

    @JsonProperty("address")
    @Size(max = 255, message = "valid_error.address_too_long")
    private String address;

    @JsonProperty("driver_licenses")
    @Size(max = 255, message = "driver.valid_error.driver_licenses_too_long")
    private String driverLicenses;

    @JsonProperty("driver_license_number")
    @Size(max = 255, message = "driver.valid_error.driver_license_number_too_long")
    @Pattern(regexp = "^\\s*\\d+\\s*$", message = "driver.valid_error.driver_license_number_is_invalid")
    private String driverLicenseNumber;

    @JsonProperty("driver_license_expiry_date")
    private Timestamp driverLicenseExpiryDate;

    @JsonProperty("know_english")
    private Boolean knowEnglish;

    @JsonProperty("ddt_certificate")
    private Boolean ddtCertificate;

    @JsonProperty("note")
    @Size(max = 500, message = "valid_error.note_too_long")
    private String note;

    @JsonProperty("active")
    @NotNull(message = "valid_error.status_is_null")
    private Boolean active;

    @JsonProperty("branch_id")
    @NotNull(message = "valid_error.branch_id_is_blank")
    private Long branchId;

    @JsonProperty("unit_operator_id")
    private Long unitOperatorId;
}
