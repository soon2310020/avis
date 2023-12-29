package com.stg.service.dto;

import javax.validation.constraints.NotNull;

import com.stg.constant.IdentificationType;
import com.stg.service.dto.constraint.Identification;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Identification
public class IdentificationDto {

    @NotNull
    private IdentificationType identificationType;

    @NotNull
    private String identificationId;

}
