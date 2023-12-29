package com.stg.service.dto.external.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

@Data
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
public class MiniTransactionReqDto {

    @NotNull(message = "Ngày sinh bắt buộc nhập")
    @Schema(description = "Ngày sinh khách hàng yyyy-mm-dd", example = "1991-01-01", required = true)
    private String dob;
    @Pattern(regexp = "MALE|FEMALE", message = "Giới tính là MALE hoặc FEMALE")
    @NotNull(message = "Giới tính bắt buộc chọn")
    @Schema(required = true)
    private String gender;
    @Schema(required = true)
    @NotNull(message = "packageCode bắt buộc chọn")
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
    @NotNull(message = "packageType bắt buộc nhập")
    private String packageType;

    @Pattern(regexp = "FIX_COMBO|FREE_STYLE")
    @Schema(description = "Type của gói bảo hiểm mua FIX_COMBO|FREE_STYLE", required = true)
    @NotNull(message = "Thông tin bắt buộc nhập")
    private String type;

    @NotNull(message = "Thông tin bắt buộc nhập")
    @Schema(description = "Mã phiên đăng nhập (Được trả về từ API xác minh token)", required = true)
    private String sessionId;

    @NotNull
    @Schema(description = "Tổng phí của gói bảo hiểm", defaultValue = "0 < totalAmount < 10 tỉ VND", required = true)
    private String totalAmount;

    @NotNull(message = "Thông tin bắt buộc nhập")
    @Schema(description = "Nội dung giao dịch", required = true)
    private String description;

    @NotNull(message = "Thông tin bắt buộc nhập")
    @Schema(description = "Phí bảo hiểm MIC", defaultValue = "0 < totalAmount < 10 tỉ VND", required = true)
    private String micAmount;

    @NotNull(message = "Thông tin bắt buộc nhập")
    @Schema(description = "Phí bảo hiểm MBAL", defaultValue = "0 < totalAmount < 10 tỉ VND", required = true)
    private String mbalAmount;

    @JsonProperty("rmPhone")
    private String rmPhone;

    @JsonProperty("isInstallment")
    private boolean isInstallment;

    @JsonProperty("transactionType")
    @NotNull(message = "Thông tin bắt buộc nhập")
    @Schema(description = "Thanh toán lần đầu hoặc gia hạn", defaultValue = "Thanh toán lần đầu", required = true)
    private String transactionType;

    @NotNull(message = "Só AF.xx bắt buộc nhập")
    @Schema(description = "Số AF.xxx của MBAL", defaultValue = "AF.XXXX", required = true)
    private String mbalAppNo;

    @Schema(description = "Số PolicyNumber của MBAL, Bắt buộc với UL2.0", defaultValue = "00000179500056443", required = true)
    private String mbalPolicyNumber;

    @NotNull(message = "Số GCNBH của MIC bắt buộc nhập")
    @Schema(description = "GCNBH của MIC", defaultValue = "1234", required = true)
    private String micContractNum;

    @Schema(description = "ID gói bảo hiểm", defaultValue = "1-16", required = true)
    @NotNull(message = "ID của gói bảo hiểm bắt buộc nhập")
    private Integer insurancePackageId;

    @Schema(description = "ID gói MBAL khi chọn FREE_STYLE", defaultValue = "1")
    private Integer mbalPackageId;

    @Schema(description = "ID gói MIC khi chọn FREE_STYLE", defaultValue = "1")
    private Integer micPackageId;

    private String micTransactionId;

    @Schema(description = "Tên gói bảo hiểm: Gói tự tin, gói phong cách", required = true)
    private String mixPackageName;

    @Schema(description = "Process mua bảo hiểm", required = true)
    @NotEmpty(message = "ProcessId bắt buộc nhập")
    private String processId;

    @Schema(description = "Auto debit")
    private boolean autoPay;
}
