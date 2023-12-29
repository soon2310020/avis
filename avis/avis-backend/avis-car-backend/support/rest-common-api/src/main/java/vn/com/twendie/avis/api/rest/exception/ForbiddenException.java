package vn.com.twendie.avis.api.rest.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.FORBIDDEN)
public class ForbiddenException extends BaseApiException {

    public ForbiddenException(String message) {
        super(message);
        super.code(HttpStatus.FORBIDDEN.value());
    }

}
