package vn.com.twendie.avis.api.rest.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class NotFoundException extends BaseApiException {

    public NotFoundException(){}

    public NotFoundException(String msg) {
        super(msg);
        super.code(HttpStatus.NOT_FOUND.value());
    }
}
