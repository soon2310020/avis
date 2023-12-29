package vn.com.twendie.avis.api.rest.exception;

public class CustomerAlreadyExistException extends BaseApiException{

    public CustomerAlreadyExistException(String msg) {
        super(msg);
    }

    public CustomerAlreadyExistException() {
        super();
    }
}
