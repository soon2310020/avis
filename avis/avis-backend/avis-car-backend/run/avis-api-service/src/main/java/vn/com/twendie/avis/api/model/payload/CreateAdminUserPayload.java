package vn.com.twendie.avis.api.model.payload;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import javax.validation.constraints.*;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class CreateAdminUserPayload {

    @JsonProperty("code")
    @NotBlank(message = "admin_user.valid_error.code_is_invalid")
    @Pattern(regexp = "NV[\\d]{4,}", message = "admin_user.valid_error.code_is_invalid")
    private String code;

    @JsonProperty("name")
    @NotBlank(message = "admin_user.valid_error.name_is_invalid")
    @Size(max = 255, message = "admin_user.valid_error.name_too_long")
    private String name;

    @JsonProperty("active")
    @NotNull(message = "valid_error.status_is_null")
    private Boolean active;

    @JsonProperty("department_id")
    @NotNull(message = "admin_user.valid_error.department_is_invalid")
    private Long departmentId;

    @JsonProperty("branch_id")
//    @NotNull(message = "valid_error.branch_id_is_blank")
    private Long branchId;

    @JsonProperty("user_role_id")
    @NotNull(message = "admin_user.valid_error.role_is_invalid")
    private Long userRoleId;

}
