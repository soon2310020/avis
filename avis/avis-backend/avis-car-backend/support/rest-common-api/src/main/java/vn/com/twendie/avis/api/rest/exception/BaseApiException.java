package vn.com.twendie.avis.api.rest.exception;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import vn.com.twendie.avis.api.rest.constant.RestCommonConstant.ErrorCode;

import java.util.Collections;

@Setter
@EqualsAndHashCode(callSuper = false)
@Accessors(fluent = true, chain = true)
public class BaseApiException extends RuntimeException {

    @Getter
    private int code = ErrorCode.DEFAULT;

    @Getter
    private String displayMessage;

    @Getter
    private Object extraData = Collections.EMPTY_MAP;

    public BaseApiException() {
        super();
    }

    public BaseApiException(String message) {
        super(message);
    }

}
