package com.stg.service3rd.baas.dto.resp;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class QueryBulkPaymentResp {
    @Schema(name = "Error code", required = false)
    private String errorCode;

    @Schema(name = "Error code", required = false)
    private List<String> errorDesc;

    @Schema(name = "Id message created by consumer", required = false)
    private String clientMessageId;

    @Schema(name = "Data trả về nếu service không lỗi", required = false)
    private Data data;

    /***/
    @Getter
    @Setter
    public static final class Data {
        @Schema(name = "Mã lô", required = false)
        private String bulkId;

        @Schema(name = "Số lượng gd", required = false)
        private String transactionNumber;

        @Schema(name = "Tổng Số gd thành công", required = false)
        private String totalSuccess;

        @Schema(name = "Trạng thái gd", required = false)
        private String bulkStatus;

        @Schema(name = "loại dịch vụ", required = false)
        private String serviceType;

        @Schema(name = "Mã lô", required = false)
        private TransactionDetail transactionDetailOutput;
    }

    /***/
    @Getter
    @Setter
    public static final class TransactionDetail {
        @Schema(name = "transactionReq", required = false)
        private String transactionReq;

        @Schema(name = "Nguồn thanh toán", required = false)
        private String sourceId;

        @Schema(name = "Loại tk thanh toán", required = false)
        private String resourceType;

        @Schema(name = "Số tk thanh toán", required = false)
        private String resourceNumber;

        @Schema(name = "tên khách hàng", required = false)
        private String customerName;

        @Schema(name = "mã bill thanh toán", required = false)
        private String transferCode;

        @Schema(name = "số tiền phí", required = false)
        private String transferFee;

        @Schema(name = "nội dung chuyển tiền", required = false)
        private String transferContent;

        @Schema(name = "Số tiền chuyển", required = false)
        private String transferAmount;

        @Schema(name = "ngày hạch toán", required = false)
        private String transferDate;

        @Schema(name = "FT number", required = false)
        private String ftNumber;

        @Schema(name = "trạng thái giao dịch con: SUCCESS|SUCCESS|SUCCESS", required = false)
        private String status;

        @Schema(name = "Mã lỗi gd con nếu có", required = false)
        private String soaErrorCode;

        @Schema(name = "Mô tả lỗi gd con nếu có", required = false)
        private String soaErrorDesc;
    }


    /**
     * ==================
     * Error code defined
     * ==================
     */
    public enum ERROR_CODE {
        OK("000", "OK"),
        PAN_IS_NOT_CARD("001", "Pan is not card number"),
        ;
        private final String code;
        private final String message;

        ERROR_CODE(String code, String message) {
            this.code = code;
            this.message = message;
        }

        public String getCode() {
            return code;
        }

        public String getMessage() {
            return message;
        }
    }
}
