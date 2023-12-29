package vn.com.twendie.avis.api.model.payload;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import vn.com.twendie.avis.api.core.constraint.IgnoreTrimReflect;
import vn.com.twendie.avis.api.core.constraint.Password;

import javax.validation.constraints.*;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class UpdateAdminUserPayload {

    @NotNull
    private Long id;

    @JsonProperty("name")
    @NotBlank(message = "admin_user.valid_error.name_is_invalid")
    @Size(max = 255, message = "admin_user.valid_error.name_too_long")
    private String name;

    @JsonProperty("password")
    @Password(message = "user.error.password_is_invalid")
    @IgnoreTrimReflect
    private String password;

    @JsonProperty("active")
    @NotNull(message = "valid_error.status_is_null")
    private Boolean active;

    @JsonProperty("branch_id")
//    @NotNull(message = "valid_error.branch_id_is_blank")
    private Long branchId;

    @JsonProperty("user_role_id")
    @NotNull(message = "admin_user.valid_error.role_is_invalid")
    private Long userRoleId;
}
