package com.stg.service3rd.baas.dto.req;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;

@Getter
@Setter
public class CollectBulkPaymentReq {

    @NotNull(message = "Thông tin bắt buộc nhập")
    @Schema(description = "Mã dịch vụ", required = true, example = "THU_HO")
    private String serviceType;

    @NotNull(message = "Thông tin bắt buộc nhập")
    @Schema(description = "Mã lô", required = true)
    private String bulkId;

    @NotNull(message = "Thông tin bắt buộc nhập")
    @Schema(description = "Tổng số giao dịch con", required = true)
    private String bulkTotalTransaction;

    @NotNull(message = "Thông tin bắt buộc nhập")
    @Schema(description = "Tổng số tiền giao dịch", required = true)
    private String bulkTotalAmount;

    @NotNull(message = "Thông tin bắt buộc nhập")
    @Schema(description = "Tổng số tiền phí", required = true)
    private String bulkTotalFee;

    @NotNull(message = "Thông tin bắt buộc nhập")
    @Schema(description = "Chi tiết thông tin gd con", required = true)
    private BulkDetail listBulkDetails;

    @Schema(description = "Chi tiết thông tin gd con")
    private String addInfos;


    /***/
    @Getter
    @Setter
    public static final class BulkDetail {
        @NotNull(message = "Thông tin bắt buộc nhập")
        @Schema(description = "Mã gd con", required = true)
        private String transactionReq;

        @NotNull(message = "Thông tin bắt buộc nhập")
        @Schema(description = "Mã nguồn thanh toán", required = true)
        private String sourceId;

        @Schema(description = "Tên khách hàng theo nguồn thanh toán")
        private String customerName;

        @NotNull(message = "Thông tin bắt buộc nhập")
        @Schema(description = "Mã bill thanh toán", required = true)
        private String transferCode;

        @NotNull(message = "Thông tin bắt buộc nhập")
        @Schema(description = "Số tiền thanh toán", required = true)
        private String transferAmount;

        @Schema(description = "Số tiền phí")
        private String transferFee;

        @NotNull(message = "Thông tin bắt buộc nhập")
        @Schema(description = "Nội dung chuyển tiền", required = true)
        private String transferContent;
    }

    /***/
    @Getter
    @Setter
    public static final class AddInfo {
        @Schema(description = "type")
        private String type;

        @Schema(description = "name")
        private String name;

        @Schema(description = "value")
        private String value;
    }
}
