package vn.com.twendie.avis.api.model.payload;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class CustomerPayload {

    @JsonProperty("id")
    private Long id;

    @JsonProperty("code")
    @Pattern(regexp = "KH[\\d]{4,}", message = "customer.valid_error.code_is_invalid")
    private String code;

    @JsonProperty("name")
    @Size(max = 255, message = "customer.valid_error.name_too_long")
    @NotBlank(message = "customer.valid_error.name_is_blank")
    private String name;

    @JsonProperty("address")
    @Size(max = 255, message = "valid_error.address_too_long")
    @NotBlank(message = "valid_error.address_is_blank")
    private String address;

    @JsonProperty("tax_code")
    @Size(max = 100, message = "customer.valid_error.tax_code_too_long")
    @NotBlank(message = "customer.valid_error.tax_code_wrong_format")
    @Pattern(regexp = "^\\s*\\d+\\s*$", message = "customer.valid_error.tax_code_wrong_format")
    private String taxCode;

    @JsonProperty("bank_account_number")
    @Size(max = 100, message = "customer.valid_error.bank_number_too_long")
//    @NotBlank(message = "customer.valid_error.bank_account_number_wrong_format")
    @Pattern(regexp = "^\\s*\\d*\\s*$", message = "customer.valid_error.bank_account_number_wrong_format")
    private String bankAccountNumber;

    @JsonProperty("bank_account_holder")
    @Size(max = 100, message = "customer.valid_error.bank_holder_too_long")
//    @NotBlank(message = "customer.valid_error.bank_account_holder_is_blank")
    private String bankAccountHolder;

    @JsonProperty("bank_name")
    @Size(max = 100, message = "customer.valid_error.bank_name_too_long")
//    @NotBlank(message = "customer.valid_error.bank_name_is_blank")
    private String bankName;

    @JsonProperty("is_new")
    private boolean isNew;

}
