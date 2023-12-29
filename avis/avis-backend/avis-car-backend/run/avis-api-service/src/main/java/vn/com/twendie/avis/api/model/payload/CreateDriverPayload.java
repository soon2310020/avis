package vn.com.twendie.avis.api.model.payload;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.*;

@EqualsAndHashCode(callSuper = true)
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class CreateDriverPayload extends DriverPayload {

    @JsonProperty("code")
    @NotBlank(message = "driver.valid_error.code_is_invalid")
    @Pattern(regexp = "TX[\\d]{4,}", message = "driver.valid_error.code_is_invalid")
    private String code;

    @JsonProperty("username")
    @NotBlank(message = "valid_error.user_name_is_invalid")
    @Pattern(regexp = "TX[\\d]{4,}", message = "valid_error.user_name_is_invalid")
    private String username;

    @JsonProperty("status")
    @NotNull(message = "driver.valid_error.status_is_blank")
    @Min(value = 0, message = "driver.valid_error.status_wrong_format")
    @Max(value = 1, message = "driver.valid_error.status_wrong_format")
    private Integer status;

    @JsonProperty("user_group_id")
    @NotNull(message = "driver.valid_error.user_group_is_blank")
    private Long userGroupId;

}
