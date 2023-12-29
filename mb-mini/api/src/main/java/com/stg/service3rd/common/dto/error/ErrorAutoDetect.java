package com.stg.service3rd.common.dto.error;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashMap;

import static com.stg.service3rd.common.utils.ApiUtil.SERVER_ERROR_MESSAGE;
import static com.stg.service3rd.common.utils.ApiUtil.UNKNOWN_ERROR_CODE;
import static com.stg.service3rd.common.utils.ApiUtil.convertMessageToString;

@Setter
@Getter
@NoArgsConstructor
public class ErrorAutoDetect extends HashMap<String, Object> implements IErrorObject {
    private int httpStatus; /*default*/

    private String errorCode;
    private String errorMessage;


    @Override
    public boolean equals(Object o) {
        if (!(o instanceof ErrorAutoDetect)) return false;
        return super.equals(o) && (httpStatus == ((ErrorAutoDetect) o).getHttpStatus());
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    /***/
    public ErrorAutoDetect add(String key, Object value) {
        this.put(key, value);
        return this;
    }


    /**
     * NOTE: Lần gọi đâu tiên sẽ được tính toán
     */
    @Override
    public String getErrorCode() {
        if (this.errorCode != null) return this.errorCode;

        // priority by ErrorCodeKeyDefault
        for (ErrorCodeKeyDefault field : ErrorCodeKeyDefault.values()) {
            Object errCode = this.get(field.field);
            if (errCode != null) {
                this.errorCode = errCode.toString();
                return this.errorCode;
            }
        }

        this.errorCode = UNKNOWN_ERROR_CODE;
        return this.errorCode;
    }


    /**
     * NOTE: Lần gọi đâu tiên sẽ được tính toán
     */
    @Override
    public String getErrorMessage() {
        if (this.errorMessage != null) return this.errorMessage;

        // priority by MessageKeyDefault
        for (MessageKeyDefault field : MessageKeyDefault.values()) {
            Object errMsg = this.get(field.field);
            if (errMsg != null) {
                this.errorMessage = convertMessageToString(errMsg);
                return this.errorMessage;
            }
        }

        this.errorMessage = SERVER_ERROR_MESSAGE;
        return this.errorMessage;
    }


    /***/
    public enum ErrorCodeKeyDefault {
        ERROR_CODE("errorCode"),
        ERROR_CODE_SN("error_code"),
        CODE("code"),
        STATUS("status"),
        ;

        private final String field;

        ErrorCodeKeyDefault(String field) {
            this.field = field;
        }
    }

    /***/
    public enum MessageKeyDefault {
        ERROR_MESSAGE("errorMessage"),
        ERROR_DESC("errorDesc"),
        ERROR_MESSAGE_SN("error_message"),
        ERROR_DESC_SN("error_desc"),
        MESSAGE("message"),
        ;

        private final String field;

        MessageKeyDefault(String field) {
            this.field = field;
        }
    }
}
