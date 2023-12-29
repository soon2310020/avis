package vn.com.twendie.avis.mobile.api.constant;

public class MobileConstant {
    public static final String DEFAULT_STARTER_PAGE = "1";
    public static final int DEFAULT_CONTRACTS_PAGE_SIZE = 10;
    public static final String MOBILE_CONTRACT_DATE_PATTERN = "dd/MM/yy";
    public static final String MOBILE_NOTIFICATION_DATE_PATTERN = "dd/MM/yyyy";

    public interface NotificationStatus {
        String SUCCESS = "success";
        String FAILED = "failed";
        String READ = "read";
    }

    public interface DriverContractStatus {
        String UPCOMING = "upcoming";
        String IN_PROGRESS = "in_progress";
        String FINISHED = "finished";
        String POST_PONE = "post_pone";
        String CANCELED = "canceled";
    }
}
