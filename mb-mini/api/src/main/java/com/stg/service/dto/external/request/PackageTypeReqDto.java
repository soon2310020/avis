package com.stg.service.dto.external.request;

import lombok.Data;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotNull;

@Data
@Accessors(chain = true)
public class PackageTypeReqDto {

    @NotNull(message = "PackageType can not be empty")
    private String packageType;

}
