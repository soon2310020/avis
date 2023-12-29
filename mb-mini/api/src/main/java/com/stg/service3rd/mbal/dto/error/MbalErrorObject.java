package com.stg.service3rd.mbal.dto.error;

import com.stg.service3rd.common.dto.error.IErrorObject;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

import static com.stg.service3rd.common.utils.LogUtil.errorDescToString;

@Getter
@Setter
@NoArgsConstructor
public class MbalErrorObject implements IErrorObject {
    private int httpStatus; /*GET from response*/

    private Integer statusCode; /*GET from body => Khi get httpStatus ưu tiên statusCode*/
    private List<String> messages;
    private String errorCode;
    private String additionalData;

    /***/
    @Override
    public String getErrorMessage() {
        return errorDescToString(messages);
    }

    @Override
    public void setErrorMessage(String errorMessage) {
        if (messages == null) messages = new ArrayList<>();
        messages.add(errorMessage);
    }

    /**
     * GET from response
     */
    public Integer getHttpStatusResp() {
        return httpStatus;
    }

    /**
     * GET from body => Khi get httpStatus ưu tiên statusCode
     */
    public int getHttpStatus() {
        if (this.statusCode == null) return httpStatus;
        return this.statusCode;
    }

}
