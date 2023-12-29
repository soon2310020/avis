package com.stg.service.dto.insurance;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter
@Setter
@Accessors(chain = true)
public class RegisterAutoDebitStatusDto {
    private boolean success;
    private String message;
}
