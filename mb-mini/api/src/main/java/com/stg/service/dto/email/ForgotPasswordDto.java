package com.stg.service.dto.email;

import lombok.Data;
import lombok.experimental.Accessors;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;


@Data
@Accessors(chain = true)
public class ForgotPasswordDto {
    @NotEmpty
    @Email
    private String emailTo;

}
