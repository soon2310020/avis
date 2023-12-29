package com.stg.service.dto.external.request;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class MbalCreateOrderReqDto {

    @NotNull(message = "Ngày sinh bắt buộc nhập")
    @Schema(description = "Ngày sinh khách hàng yyyy-mm-dd", example = "1991-01-01", required = true)
    private String dob;
    @Pattern(regexp = "MALE|FEMALE", message = "Giới tính là MALE hoặc FEMALE")
    @NotNull(message = "Giới tính bắt buộc chọn")
    @Schema(required = true)
    private String gender;
    @Schema(required = true)
    private String packageCode;
    @NotNull(message = "Mã nhóm nghề nghiệp bắt buộc nhập")
    @Schema(required = true)
    private Integer occupationClass;
    @Schema(required = true)
    private String packageName;
    @NotEmpty(message = "Email bắt buộc nhập")
    @Schema(required = true)
    private String email;
    @Schema(required = true, example = "INVEST, PROTECT")
    @NotNull(message = "Package type bắt buộc nhập")
    private String packageType;
    @Schema(description = "Phí bảo hiểm MBAL", required = true)
    @NotNull(message = "Phí bảo hiểm MBAL bắt buộc nhập")
    private Long amount;
    private String phoneRefer;
    private String rmCodeRefer;
    private String rmNameRefer;
    @Schema(description = "Số AF.xxx", required = true)
    @NotNull(message = "AppNo bắt buộc nhập")
    private String appNo;
    @Schema(description = "Số Policy", required = true)
    @NotNull(message = "PolicyNumber bắt buộc nhập")
    private String policyNumber;
}
