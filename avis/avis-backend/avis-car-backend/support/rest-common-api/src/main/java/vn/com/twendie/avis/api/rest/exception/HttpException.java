package vn.com.twendie.avis.api.rest.exception;

import org.springframework.web.bind.annotation.ResponseStatus;

import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

@ResponseStatus(INTERNAL_SERVER_ERROR)
public class HttpException extends BaseApiException {

    public HttpException(String msg) {
        super(msg);
        super.code(INTERNAL_SERVER_ERROR.value());
    }

}
