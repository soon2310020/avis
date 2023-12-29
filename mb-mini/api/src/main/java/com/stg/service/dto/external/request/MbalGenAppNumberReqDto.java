package com.stg.service.dto.external.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

@Data
@Accessors(chain = true)
public class MbalGenAppNumberReqDto {

    @Schema(description = "Ngày sinh", example = "1991-12-20")
    private String dob;
    @Pattern(regexp = "MALE|FEMALE", message = "Giới tính là MALE hoặc FEMALE")
    @NotNull(message = "Giới tính bắt buộc chọn")
    private String gender;
    private String occupation;
    private Integer occupationClass;
    private String productCode;
    private String productName;
    private String insuranceFee;
    @Schema(example = "Việt Nam", description = "Quốc gia")
    private String nationality;
    private String packageCode;
    private String packageName;
    private String message;
    private String phoneRefer;
    private String rmCodeRefer;
    private String rmNameRefer;
    private String supporterCode;
    private String supporterName;
    private long amount;
    private String email;
    private String phone;
    @Schema(description = "packageType", example = "INVEST")
    private String packageType;


    @NotEmpty(message = "Address must not be empty")
    private String address;

}
