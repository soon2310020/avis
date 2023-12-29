package com.stg.service.dto.external.request;

import lombok.Data;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

@Data
@Accessors(chain = true)
public class MbalPackageProductReqDto {

    @NotNull
    private String packageCode;
    @Pattern(regexp = "MALE|FEMALE", message = "Gender must be MALE or FEMALE")
    @NotNull
    private String gender;
    private String age;
    @NotNull
    private Integer occupationClass;
    @NotNull
    private String packageType;
}
