package com.stg.service.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import javax.validation.constraints.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class UserEditingDto {

    @Size(max = 100)
    @Email
    @NotEmpty
    @Schema(required = true)
    private String email;
    @Pattern(regexp = "^(?=\\s*\\S).*$", message = "Firstname must not be empty")
    @Schema(required = true)
    private String firstName;
    @Pattern(regexp = "^(?=\\s*\\S).*$", message = "Lastname must not be empty")
    @Schema(required = true)
    private String lastName;
    @NotNull(message = "Thông tin bắt buộc nhập")
    @Schema(required = true)
    private Boolean enabled;

}
