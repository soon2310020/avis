package saleson.common.infrastructure.exception;


public class EmailException extends DomainException {
    private static final int DOMAIN_CODE = 7;

    public EmailException(int errorCode, String message) {
        super(DOMAIN_CODE, errorCode, message);
    }

    public EmailException(int errorCode, String message, Throwable e) {
        super(DOMAIN_CODE, errorCode, message, e);
    }
}
