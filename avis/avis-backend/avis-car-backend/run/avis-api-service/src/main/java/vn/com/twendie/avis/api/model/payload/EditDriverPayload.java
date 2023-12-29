package vn.com.twendie.avis.api.model.payload;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import vn.com.twendie.avis.api.core.constraint.IgnoreTrimReflect;
import vn.com.twendie.avis.api.core.constraint.Password;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

@EqualsAndHashCode(callSuper = true)
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class EditDriverPayload extends DriverPayload {

    @JsonProperty("id")
    @NotNull
    private Long id;

    @JsonProperty("password")
    @Password(message = "user.error.password_is_invalid")
    @IgnoreTrimReflect
    private String password;

    @JsonProperty("status")
    @NotNull(message = "driver.valid_error.status_is_blank")
    @Min(value = 0, message = "driver.valid_error.status_wrong_format")
    @Max(value = 2, message = "driver.valid_error.status_wrong_format")
    private Integer status;

}
