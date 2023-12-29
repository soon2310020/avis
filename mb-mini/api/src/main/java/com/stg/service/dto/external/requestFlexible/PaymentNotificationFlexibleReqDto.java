package com.stg.service.dto.external.requestFlexible;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class PaymentNotificationFlexibleReqDto {

    @NotNull(message = "Giá trị bắt buộc nhập")
    @Schema(name = "Số AF.xxx", required = true)
    private String applicationNumber;
    @NotNull(message = "Giá trị bắt buộc nhập")
    @Schema(name = "Trạng thái chi hộ sang MBAL", required = true, example = "true")
    private boolean success;
    private String note;

    @NotNull(message = "Giá trị bắt buộc nhập")
    @Valid
    private Transaction transaction;

    @Valid
    private Source source;

    @Data
    @Accessors(chain = true)
    @AllArgsConstructor
    @NoArgsConstructor
    @Schema
    public static class Transaction {
        @NotNull(message = "Giá trị bắt buộc nhập")
        @Schema(name = "Tổng số tiền bảo hiểm MBAL", required = true, example = "10000000")
        private BigDecimal amount;

        @NotNull(message = "Giá trị bắt buộc nhập")
        @Schema(name = "ID giao dich MB", required = true, example = "AWxxxx")
        private String code;

        private String description;
        @Schema(name = "Thời gian thanh toán thành công. Chuẩn ISO_DATE", required = true)
        private String completedAt;
        @Schema
        private String paymentId;

    }

    @Data
    @Accessors(chain = true)
    @AllArgsConstructor
    @NoArgsConstructor
    @Schema
    @Valid
    public static class Source {
        @Schema
        private String phoneNumber;
        @Schema
        private String accountId;

        @NotNull(message = "Giá trị bắt buộc nhập")
        @Schema(name = "Mã khách hàng MB", required = true)
        private String cif;
        @Schema
        private String name;

    }

}
