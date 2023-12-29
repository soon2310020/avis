package com.stg.service.dto.external.requestFlexible;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotNull;

@Data
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
public class FlexibleTransactionReqDto {
    @NotNull(message = "Thông tin bắt buộc nhập")
    @Schema(description = "Mã phiên đăng nhập (Được trả về từ API xác minh token)", required = true)
    private String sessionId;

    @NotNull(message = "Thông tin bắt buộc nhập")
    @Schema(description = "Nội dung giao dịch", required = true)
    private String description;

    @JsonProperty("transactionType")
    @NotNull(message = "Thông tin bắt buộc nhập")
    @Schema(description = "Thanh toán lần đầu hoặc gia hạn", defaultValue = "Thanh toán lần đầu", required = true)
    private String transactionType;

    @Schema(description = "ID gói bảo hiểm", defaultValue = "17", required = true)
    @NotNull(message = "ID của gói bảo hiểm bắt buộc nhập")
    private Integer insurancePackageId;

    @Schema(description = "Tên gói bảo hiểm: Gói tự tin, gói phong cách", required = true)
    private String mixPackageName;

    @Schema(description = "Process mua bảo hiểm", required = true)
    @NotNull(message = "processId bắt buộc nhập")
    private Integer processId;

    private boolean isInstallment;

    @Schema(description = "Auto debit")
    private boolean autoPay;
}
