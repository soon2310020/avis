package vn.com.twendie.avis.api.model.payload;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.com.twendie.avis.api.core.constraint.IgnoreTrimReflect;
import vn.com.twendie.avis.api.core.constraint.Password;

import javax.validation.constraints.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class AdminCustomerPayload {

    @JsonProperty("id")
    private Long id;

    @JsonProperty("code")
    @NotNull(message = "customer.valid_error.code_is_invalid")
    @Pattern(regexp = "AD[\\d]{4,}", message = "customer.valid_error.code_is_invalid")
    private String code;

    @JsonProperty("name")
    @Size(max = 100, message = "member_customer.valid_error.name_too_long")
    @NotBlank(message = "member.valid_error.name_is_blank")
    private String name;

    @JsonProperty("department")
    @Size(max = 100, message = "customer.valid_error.position_too_long")
    @NotBlank(message = "member.valid_error.position_is_blank")
    private String department;

    @JsonProperty("mobile")
    @Size(max = 15, message = "valid_error.phone_number_too_long")
    @NotBlank(message = "valid_error.phone_number_wrong_format")
    @Pattern(regexp = "^\\s*\\d+\\s*$", message = "valid_error.phone_number_wrong_format")
    private String mobile;

    @JsonProperty("country_code")
    @NotBlank(message = "valid_error.country_code_is_blank")
    private String countryCode;

    @JsonProperty("iso2")
    @NotBlank(message = "valid_error.iso2_is_blank")
    private String iso2;

    @JsonProperty("email")
    @Size(max = 100, message = "valid_error.email_too_long")
    @NotBlank(message = "valid_error.email_wrong_format")
    @Email(regexp = "^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,4}$", flags = Pattern.Flag.CASE_INSENSITIVE, message = "valid_error.email_wrong_format")
    private String email;

    @JsonProperty("is_new")
    private boolean isNew;

    @JsonProperty("password")
    @Password(message = "user.error.password_is_invalid")
    @IgnoreTrimReflect
    private String password;

    @JsonProperty("active")
    @NotNull(message = "valid_error.status_is_null")
    private Boolean active;

}
