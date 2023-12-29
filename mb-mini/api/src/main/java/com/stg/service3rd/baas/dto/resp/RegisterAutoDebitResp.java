package com.stg.service3rd.baas.dto.resp;

import com.stg.service3rd.common.utils.LogUtil;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * NON-OTP
 */
@Getter
@Setter
public class RegisterAutoDebitResp {
    @Schema(name = "Error code", required = false)
    private String errorCode;

    @Schema(name = "Error code", required = false)
    private List<String> errorDesc;

    @Schema(name = "Id message created by consumer", required = false)
    private String clientMessageId;

    @Schema(name = "Data returned if no error", required = false)
    private Data data;

    /***/
    public String getErrorMessage() {
        return LogUtil.errorDescToString(errorDesc);
    }


    /***/
    @Getter
    @Setter
    public static final class Data {
        @Schema(name = "Status of result", required = true)
        private String status;

        @Schema(name = "A uuid generated string", required = true)
        private String sourceId;
    }

}
