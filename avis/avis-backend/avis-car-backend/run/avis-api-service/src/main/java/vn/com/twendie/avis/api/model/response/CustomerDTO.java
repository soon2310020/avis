package vn.com.twendie.avis.api.model.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class CustomerDTO {

    @JsonProperty("customer_id") private Long id;
    @JsonProperty("code") private String code;
    @JsonProperty("name") private String name;
    @JsonProperty("position") private String position;
    @JsonProperty("phone_number") private String mobile;
    @JsonProperty("country_code") private String countryCode;
    @JsonProperty("email") private String email;
    @JsonProperty("address") private String address;
    @JsonProperty("tax_code") private String taxCode;
    @JsonProperty("bank_account_number") private String bankAccountNumber;
    @JsonProperty("bank_account_holder") private String bankAccountHolder;
    @JsonProperty("bank_name") private String bankName;
    @JsonProperty("bank_branch") private String bankBranch;
    @JsonProperty("iso2") private String iso2;
    @JsonProperty("id_card") private String idCard;
    @JsonProperty("card_type") private Integer cardType;

    @JsonProperty("admin_id") private Long adminId;
    @JsonProperty("admin_name") private String adminName;

    @JsonProperty("customer_type")
    private CustomerTypeDTO customerType;

    @JsonProperty("created_by")
    private UserDTO createdBy;

    @JsonProperty("created_at")
    private Timestamp createdAt;

    @JsonProperty("in_contract")
    private boolean inContract;

    @JsonProperty("active")
    private boolean active;

}
