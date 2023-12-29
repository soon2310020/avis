package com.stg.service3rd.baas.dto.resp;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class CollectBulkPaymentResp {
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
        @Schema(name = "Mã lô", required = true)
        private String bulkId;

        @Schema(name = "Trạng thái lô", required = true)
        private String bulkStatus;

        @Schema(name = "Mã lỗi lô", required = true)
        private String bulkErrorCode;

        @Schema(name = "Mô tả lỗi lô", required = true)
        private String bulkErrorDesc;
    }


    /**
     * ==================
     * Error code defined
     * ==================
     */
    public enum ERROR_CODE {
        OK("000", "OK"),
        TIME_OUT("2030", "ServiceType invalid"),
        UNKNOWN_ERROR("40054", "Bulk id invalid or max length exceed"),
        REGISTER_NUMBER_INVALID("40050", "The number of transaction is invalid or exceed"),
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
