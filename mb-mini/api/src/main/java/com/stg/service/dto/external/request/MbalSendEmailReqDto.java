package com.stg.service.dto.external.request;

import lombok.Data;
import lombok.experimental.Accessors;

import javax.validation.constraints.Email;

@Data
@Accessors(chain = true)
public class MbalSendEmailReqDto {

    @Email
    private String mail;

}
