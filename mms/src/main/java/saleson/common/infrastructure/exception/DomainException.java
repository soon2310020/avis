package saleson.common.infrastructure.exception;


public class DomainException extends RuntimeException {
    public static final int GENERAL_ERROR = 500;
    public static final int UNKNOWN_ERROR = 99;
    public static final int UNIT_SIZE = 1010;
    private static final long serialVersionUID = 5262974602102886958L;
    private int domainCode;

    private int errorCode;

    private DomainException() {
    }

    public DomainException(int errorCode, Throwable e) {
        super(e);
        this.domainCode = Domain.General.getDomainCode();
        this.errorCode = errorCode;
    }

    public DomainException(int domainCode, int errorCode, String message) {
        super(message);
        this.domainCode = domainCode;
        this.errorCode = errorCode;
    }

    protected DomainException(int domainCode, int errorCode, String message, Throwable e) {
        super(message, e);
        this.domainCode = domainCode;
        this.errorCode = errorCode;
    }

    public DomainException(String message) {
        super(message);
        this.domainCode = Domain.General.getDomainCode();
    }

    public static int calculateCode(int domainCode, int errorCode) {
        return domainCode * UNIT_SIZE + errorCode;
    }

    public int getCode() {
        return calculateCode(domainCode, errorCode);
    }

    public Error getErrorResponse() {
        return new Error(this.getCode(), super.getMessage());
    }

    // @formatter:off
    public enum Domain {
        General(1),
        User(2),
        Mail(3);

        private int domainCode;

        Domain(int domainCode) {
            this.domainCode = domainCode;
        }

        public int getDomainCode() {
            return domainCode;
        }
    }

    // @formatter:on
    public static class Error {
        private int error;
        private String error_description;

        public Error() {
        }

        public Error(int error, String error_description) {
            this.error = error;
            this.error_description = error_description;
        }

        public int getError() {
            return error;
        }

        public void setError(int error) {
            this.error = error;
        }

        public String getError_description() {
            return error_description;
        }

        public void setError_description(String error_description) {
            this.error_description = error_description;
        }

    }

}
