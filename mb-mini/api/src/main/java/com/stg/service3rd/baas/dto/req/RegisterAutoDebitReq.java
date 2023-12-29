package com.stg.service3rd.baas.dto.req;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;

@Getter
@Setter
public class RegisterAutoDebitReq {

    @NotNull(message = "Thông tin bắt buộc nhập")
    @Schema(description = "Mã dịch vụ", required = true)
    private String serviceCode;

    @NotNull(message = "Thông tin bắt buộc nhập")
    @Schema(description = "Type of customer: CA_NHAN||DOANH_NGHIEP", required = true)
    private String cusType;

    @NotNull(message = "Thông tin bắt buộc nhập")
    @Schema(description = "Personal identify")
    private String nationalId;

    @Schema(description = "Registration number of enterprise")
    private String registrationNumber;

    @NotNull(message = "Thông tin bắt buộc nhập")
    @Schema(description = "Type of customer account: ACCOUNT, CARD||WALLET||RESOURCEID", required = true)
    private String sourceType;

    @NotNull(message = "Thông tin bắt buộc nhập")
    @Schema(description = "Customer account number", required = true)
    private String sourceNumber;

    @NotNull(message = "Thông tin bắt buộc nhập")
    @Schema(description = "Customer account name", required = true)
    private String sourceName;

    @NotNull(message = "Thông tin bắt buộc nhập")
    @Schema(description = "Customer phone number", required = true)
    private String phoneNumber;

    @NotNull(message = "Thông tin bắt buộc nhập")
    @Schema(description = "Customer email for received information", required = true)
    private String email;

}
