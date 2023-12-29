package com.stg.utils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

public final class Endpoints {

    private Endpoints() {
    }

    public static final String API = "/api";
    public static final String SIGN_IN = "/signin";
    public static final String USER = "/users";
    public static final String DETAIL = "/detail";
    public static final String FEATURE = "/feature";
    public static final String FORGOT_PASSWORD = "/forgotPassword";
    public static final String REVOKE = "/revoke";
    public static final String CHANGE_PASSWORD = "/changePassword";
    public static final String INSURANCE = "/insurance";
    public static final String CUSTOMERS = "/customers";
    public static final String EXPORT_CUSTOMER_LIST = "/exportCustomers";
    public static final String EXPORT_CUSTOMER = "/exportCustomer";
    public static final String DASHBOARD = "/dashboard";
    public static final String OVERVIEW = "/overview";
    public static final String RELEASE_QUANTITY = "/releaseQuantity";
    public static final String INSURANCE_PACKAGE = "/insurancePackages";
    public static final String CUSTOMER_HEALTHY = "/customerHealthy";
    public static final String ME = "/me";

    public static final String PACKAGE = "/package";
    public static final String CONTRACT = "/contract";
    public static final String REQUEST = "/request";
    public static final String ILLUSTRATION = "/illustration";
    public static final String PAYMENT = "/payment";

    public static final String URL_ID = "/{id}";
    public static final String EXPORT = "/export";
    public static final String OCCUPATION = "/occupation";
    public static final String IMPORT = "/import";
    public static final String HEALTH_QUESTION = "/healthQuestion";
    public static final String TWELVE_QUESTION = "/twelveQuestion";
    public static final String CAMPAIGN = "/campaign";
    public static final String UPLOAD = "/upload";
    public static final String DOWNLOAD = "/download";
    public static final String IMAGE = "/image";
    public static final String PACKAGE_PHOTO = "/packagePhoto";

    public static final String RESET = "/reset";
    public static final String CONFIRM = "/confirmation";
    private static final String PAY_ON_BEHALF = "/pay-on-behalf";
    private static final String IC = "/ic";

    public static final String DOCUMENT = "/document";

    // sign in
    public static final String URL_SIGN_IN = API + SIGN_IN;
    public static final String URL_FORGOT_PASSWORD = API + FORGOT_PASSWORD;

    // user
    public static final String URL_USER_DETAIL = API + USER  + URL_ID;
    public static final String URL_USER_REVOKE = API + USER + REVOKE;
    public static final String URL_USER_LIST = API + USER;
    public static final String URL_USER_ME = API + USER + ME;

    public static final String URL_FEATURE = API + USER + FEATURE;

    public static final String URL_CHANGE_PASSWORD = API + USER + CHANGE_PASSWORD;
    public static final String URL_RESET_PASSWORD_REQUEST = API + USER + CHANGE_PASSWORD + RESET;
    public static final String URL_RESET_PASSWORD_CONFIRM = API + USER + CHANGE_PASSWORD + CONFIRM;

    // MiniApp
    public static final String MINI_APP_LIST_CONTRACTS = "/external/mini/contracts";
    public static final String MINI_APP_DETAIL_CONTRACT = "/external/mini/contract/{contractId}";
    public static final String MINI_APP_CUSTOMER = "/external/mini/customer";
    public static final String MINI_APP_UPLOAD_FILE = "/external/mini/upload-files";
    public static final String MINI_ILLUSTRATION_TABLE_CREATE = "/external/mini/illustration/{processId}";
    public static final String MINI_INSTALLMENT_CONFIRM = "/external/mini/installment/confirm";

    // Call to CRM
    public static final String EXTERNAL_CRM_GET_VALUE= "/external/crm/employees/short";

    // Call to External MB
    public static final String EXTERNAL_MB_SESSION_VERIFY = "/external/mb/session/verify";
    public static final String EXTERNAL_MB_START_TRANSACTION = "/external/mb/transaction";
    public static final String EXTERNAL_MB_CALLBACK_TRANSACTION = "/external/mb/callback/transaction";

    // Call to External MBAL
    public static final String EXTERNAL_MBAL_PACKAGE_INFO = "/external/mbal/package/info";
    public static final String EXTERNAL_MBAL_CUSTOMER_INFO = "/external/mbal/customer/info";
    public static final String EXTERNAL_MBAL_PACKAGE_PRODUCT= "/external/mbal/package-product";
    public static final String EXTERNAL_MBAL_BMH_SUMMARY = "/external/mbal/pav";
    public static final String EXTERNAL_MBAL_SEND_EMAIL = "/external/mbal/sent-email";
    public static final String EXTERNAL_MBAL_ALL_CATEGORY = "/external/mbal/all-category";
    public static final String EXTERNAL_MBAL_VIEW_ILLUSTRATION_BOARD = "/external/mbal/view-detail";

    // Call to External MIC
    public static final String EXTERNAL_MIC_INSURANCE_RESULT = "/external/mic/gcn_miccare_phi";
    public static final String EXTERNAL_MIC_SEARCH_INSURANCE_CERT = "/external/mic/gcn_tracuu";

    //External-MB
    public static final String MB_VALIDATE_TOKEN = "/api/merchant/v1/session/verify";
    public static final String MB_CREATE_MERCHANT_TOKEN = "/api/merchant/v1/session/create-merchant-token";
    public static final String MB_START_TRANSACTION = "/api/merchant/v1/transaction";
    public static final String MB_GET_TRANSACTION = "/api/merchant/v1/transaction/%s";

    // External-MBAL
    // Lấy thông tin Quyền lợi của các gói sản phẩm
    public static final String MBAL_GENERATE_ACCESS_TOKEN = "/api/authentication/sso-server/login";
    // Lấy thông tin Quyền lợi của các gói sản phẩm
    public static final String MBAL_PACKAGES_INFO = "/api/ul2020/packages";
    // Check thông tin khách hàng - Tuổi + Nghề nghiệp
    public static final String MBAL_CHECK_CUSTOMER_INFO = "/api/ul2020/check-customer-info";
    // Lấy thông tin chi tiết BMH sau khi gửi kèm thông tin user
    public static final String MBAL_BMH_GENERATE_QUOTE = "/api/ul2020/generate-quote";
    public static final String MBAL_BMH_PACKAGE_PRODUCT = "/api/ul2020/package-product";
    public static final String MBAL_BMH_CREATE_ORDER = "/api/ul2020/create-order";
    // Tóm tắt bảng minh hoạ
    public static final String MAL_BMH_SUMMARY = "/api/ul2020/pav";
    // Gửi email bảng minh hoạ
    public static final String MBAL_BMH_SEND_EMAIL = "/api/ul2020/sent-mail";
    // Lấy thông tin 3 câu hỏi sức khoẻ
    public static final String MBAL_THREE_QUESTION_INFO = "/api/ul2020/all-category";
    public static final String MBAL_VIEW_ILLUSTRATION_BOARD = "/api/ul2020/view-detail";
    public static final String MBAL_EMPLOYEE_SET = "/api/ul2020/employee-set";
    public static final String MBAL_VALIDATE_BP= "/api/ul2020/validate-bp";
    public static final String MBAL_GENERATE_APP_NUMBER= "/api/ul2020/generate-application-number";
    public static final String MBAL_MARK_ORDER_AS_PAID = "/api/payment-hub/mark-order-as-paid";

    //External-MIC
    // API trả kết quả biểu phí bảo hiểm
    public static final String MIC_INSURANCE_RESULT = "/api/gcn_miccare_phi";
    public static final String MIC_GENERATE_INSURANCE_CERT = "/api/GCN_MICCARE";
    public static final String MIC_SEARCH_INSURANCE_CERT = "/api/gcn_tracuu";

    //External-BAAS
    public static final String BAAS_GENERATE_TOKEN = "/private/oauth2/v1/token";
    public static final String BAAS_MAKE_TRANSFER_PARTNER = "/private/ms/funds-partner/transfer-fund/v1.0/make-transfer-partner";
    public static final String BAAS_CHECK_STATUS_TRAN = "private/ms/bank-info/v1.0/transaction/trans-status";
    public static final String MINI_APP_INSERT_UPDATE_UL2020_QUESTION = "/external/mini/insert-or-update-ul2020-question";
    public static final String INSERT_UPDATE_UL2020_QUESTION = "/api/ul2020/insert-or-update-ul2020-question";

    public static final String CRM_CORE_GENERATE_TOKEN = "/ms/crmcore-authorization/v1.0/token/serviceAccount";
    public static final String CRM_CORE_VALUE = "/ms/crmcore-rm-profile/v1.0/employees/short/rmCode";

    // External MBAL V2
    public static final String MBAL_BMH_CREATE_PROCESS = "/api/v1/ullp/processes";
    public static final String MBAL_BMH_CREATE_QUOTE = "/api/v1/ullp/processes/%s/quotation";
    public static final String MBAL_CONFIRM_QUOTATION = "/api/v1/ullp/processes/%s/quotation/confirmation";
    public static final String MBAL_SUBMIT_APPLICATION = "/api/v1/ullp/processes/%s/application/submission";
    public static final String MBAL_LIST_OCCUPATION = "/api/v1/occupations";
    public static final String MBAL_LIST_PACKAGE = "/api/v1/ullp/packages";
    public static final String MBAL_SEND_MAIL = "/api/v1/ullp/processes/%s/quotation/email";
    public static final String MBAL_DOWNLOAD_FILE_BMH = "/api/v1/ullp/processes/%s/quotation/file";

    public static final String EXTERNAL_MBAL_BMH_CREATE_PROCESS = "/external/ullp/processes";
    public static final String EXTERNAL_MBAL_BMH_CREATE_QUOTE = "/external/ullp/processes/{processId}/quotation";
    public static final String EXTERNAL_MBAL_CONFIRM_QUOTATION = "/external/ullp/processes/{processId}/quotation/confirmation";
    public static final String EXTERNAL_MBAL_SUBMIT_APPLICATION = "/external/ullp/processes/{processId}/application/submission";
    public static final String EXTERNAL_MBAL_LIST_OCCUPATION = "/external/occupations";
    public static final String EXTERNAL_MBAL_LIST_PACKAGE = "/external/ullp/packages";
    public static final String EXTERNAL_MBAL_POLICY_SYNC = "/external/policy/mbal/sync";
    public static final String EXTERNAL_MBAL_SEND_MAIL = "/external/ullp/process/{processId}/quotation/email";
    public static final String EXTERNAL_MBAL_DOWNLOAD_FILE_BMHL = "/external/ullp/processes/{processId}/quotation/file";
    public static final String MBAL_PAYMENT_NOTIFICATION = "/api/v1/ullp/payment-notifications";
    public static final String PAV = "/pav";
    public static final String EXTERNAL = "/external";
    public static final String  URL_MBAL_PACKAGE_LIST_NO_PAGING = "/api/package/mbal";
    public static final String  URL_MIC_PACKAGE_LIST_NO_PAGING = "/api/package/mic";
    public static final String EXTERNAL_GET_IC = "/external/getIC";

    // Quản lý gói bảo hiểm
    public static final String  URL_INSURANCE_PACKAGE_LIST = API + INSURANCE + PACKAGE;
    public static final String  URL_INSURANCE_DETAIL = API + INSURANCE + PACKAGE + DETAIL + URL_ID;
    public static final String URL_INSURANCE_EXPORT = URL_INSURANCE_PACKAGE_LIST + EXPORT;
    //QUẢN LÝ KHÁCH HÀNG
    public static final String  URL_CUSTOMER_LIST_EXPORT = API + CUSTOMERS + EXPORT_CUSTOMER_LIST;
    public static final String  URL_CUSTOMER_LIST_EXPORT_DETAIL = API + CUSTOMERS + EXPORT_CUSTOMER + DETAIL + URL_ID ;
    public static final String  URL_CUSTOMER_LIST = API + CUSTOMERS;
    public static final String  URL_CUSTOMER_DETAIL = API + CUSTOMERS + URL_ID ;

    // Quản lý HDBH
    public static final String URL_INSURANCE_CONTRACT_LIST = API + INSURANCE + CONTRACT;
    public static final String URL_INSURANCE_CONTRACT_DETAIL = API + INSURANCE + CONTRACT + DETAIL + URL_ID;
    public static final String URL_INSURANCE_CONTRACT_LIST_EXPORT = API + INSURANCE + CONTRACT + EXPORT;

    public static final String URL_INSURANCE_CONTRACT_THIRD_PARTY_LIST = API + INSURANCE + CONTRACT + "/third-party";
    public static final String URL_INSURANCE_CONTRACT_THIRD_PARTY_EXPORT_LIST = API + INSURANCE + CONTRACT + "/third-party/export";
    public static final String URL_INSURANCE_CONTRACT_THIRD_PARTY_DETAIL = API + INSURANCE + CONTRACT + "/third-party" + DETAIL + URL_ID;

    // Quản lý yêu cầu BH
    public static final String  URL_INSURANCE_REQUEST_LIST = API + INSURANCE + REQUEST;
    public static final String  URL_INSURANCE_REQUEST_DETAIL = API + INSURANCE + REQUEST + DETAIL + URL_ID;
    public static final String  URL_INSURANCE_REQUEST_LIST_EXPORT = API + INSURANCE + REQUEST + EXPORT;

    // Quản lý BMH
    public static final String  URL_ILLUSTRATION_TABLE_LIST = API + ILLUSTRATION ;
    public static final String  URL_ILLUSTRATION_TABLE_DETAIL = API + ILLUSTRATION + DETAIL + URL_ID;
    public static final String  URL_ILLUSTRATION_TABLE_LIST_EXPORT = API + ILLUSTRATION + EXPORT;

    // Quản lý Thanh toán
    public static final String  URL_INSURANCE_PAYMENT_LIST = API + INSURANCE + PAYMENT;
    public static final String  URL_INSURANCE_PAYMENT_DETAIL = API + INSURANCE + PAYMENT + DETAIL + URL_ID;
    public static final String  URL_INSURANCE_PAYMENT_LIST_EXPORT = API + INSURANCE + PAYMENT + EXPORT;
    public static final String  URL_UPDATE_CONTROL_STATE_INSURANCE_PAYMENT = API + INSURANCE + PAYMENT + URL_ID + "/control-State";
    public static final String  URL_INSURANCE_PAYMENT_WAITING_LIST = API + INSURANCE + PAYMENT + "/waiting";
    public static final String  URL_INSURANCE_PAYMENT_WAITING_DETAIL = API + INSURANCE + PAYMENT + "/waiting/{id}";
    public static final String  URL_INSURANCE_PAYMENT_WAITING_EXPORT = API + INSURANCE + PAYMENT + "/waiting-export";

    // Nghề nghiệp
    public static final String URL_OCCUPATION_LIST = API + OCCUPATION;
    public static final String URL_OCCUPATION_IMPORT = API + OCCUPATION + IMPORT;
    public static final String URL_PAV_IMPORT = API + PAV + IMPORT;
    public static final String URL_LIST_PAV = EXTERNAL + PAV;

    public static final String URL_OCCUPATION_HISTORY_LIST = API + OCCUPATION + "/history";

    // Danh sách câu hỏi và cam kết
    public static final String URL_HEALTH_QUESTION = API + HEALTH_QUESTION;
    public static final String URL_HEALTH_TWELVE_QUESTION = API + HEALTH_QUESTION + TWELVE_QUESTION;

    // Quản lý chiến dịch
    public static final String URL_CAMPAIGN_LIST_EXPORT = API + CAMPAIGN + EXPORT;
    public static final String URL_CAMPAIGN = API + CAMPAIGN;
    public static final String URL_CAMPAIGN_DETAIL = API + CAMPAIGN + URL_ID ;

    //Upload
    public static final String URL_UPLOAD_IMAGE = API + UPLOAD + IMAGE;
    public static final String URL_DOWNLOAD_IMAGE = API + DOWNLOAD + IMAGE;
    public static final String URL_DOWNLOAD_BAAS = API + DOWNLOAD + "/forControl";

    //dashboard
    public static final String URL_DASHBOARD_OVERVIEW = API + DASHBOARD + OVERVIEW;
    public static final String URL_DASHBOARD_RELEASE_QUANTITY = API + DASHBOARD + RELEASE_QUANTITY;
    public static final String URL_DASHBOARD_INSURANCE_PACKAGE = API + DASHBOARD + INSURANCE_PACKAGE;
    public static final String URL_DASHBOARD_CUSTOMER_HEALTHY = API + DASHBOARD + CUSTOMER_HEALTHY;

    // Back-office
    public static final String URL_PACKAGE_PHOTO = API + PACKAGE_PHOTO;
    public static final String URL_PAY_ON_BEHALF_MANUAL = API + PAY_ON_BEHALF;
    public static final String URL_CREATE_INSTALLMENT_MANUAL = API + "/installment/create-manual";
    public static final String URL_CALLBACK_MANUAL = API + INSURANCE + PAYMENT + "/callback-manual";
    public static final String URL_CHECK_TRANSACTION = API + INSURANCE + PAYMENT + "/check";

    public static final String URL_IMPORT_IC = API + IC + IMPORT;
    public static final String URL_LIST_IC = API + IC;
    public static final String URL_LIST_IC_HISTORY = API + IC + "/history";

    // External Flexible
    public static final String EXTERNAL_FX_MIC_PACKAGES = "/external/mic/packages";
    public static final String EXTERNAL_FX_CREATE_PROCESS = "/external/flexible/processes";
    public static final String EXTERNAL_FX_CREATE_QUOTATION = "/external/flexible/create-quotation";
    public static final String EXTERNAL_FX_SEND_MAIL = "/external/flexible/send-mail";
    public static final String EXTERNAL_FX_CONFIRM_QUOTATION = "/external/flexible/confirm-quotation";
    public static final String EXTERNAL_FX_SUBMIT = "/external/flexible/submit";
    public static final String EXTERNAL_FX_DOWNLOAD_PDF = "/external/flexible/complete-pdf";
    public static final String EXTERNAL_FX_ADD_ASSURED = "/external/flexible/add-assured/{processId}";
    public static final String EXTERNAL_FX_GET_POLICY = "/external/flexible/policy";
    public static final String EXTERNAL_FX_UPLOAD_FILES = "/external/flexible/files/{processId}";
    public static final String EXTERNAL_FX_START_TRANSACTION = "/external/flexible/transaction";
    public static final String EXTERNAL_FX_MIC_CONTRACT = "/external/mic/contract";

    // Flexible-MBAL
    public static final String MBAL_FLEXIBLE_CREATE_PROCESS = "/api/v1.1/open/product/ulrp30/processes";
    public static final String MBAL_FLEXIBLE_CREATE_QUOTE = "/api/v1.1/open/product/ulrp30/processes/%s/quotation";
    public static final String MBAL_FLEXIBLE_CONFIRM_QUOTE = "/api/v1.1/open/product/ulrp30/processes/%s/quotation/confirmation";
    public static final String MBAL_FLEXIBLE_SUBMIT_APP = "/api/v1.1/open/product/ulrp30/processes/%s/application/submission";
    public static final String MBAL_FLEXIBLE_SEND_MAIL = "/api/v1.1/open/product/ulrp30/processes/%s/quotation/email";
    public static final String MBAL_FLEXIBLE_NOTICE_PAYMENT = "/api/v1.1/open/product/ulrp30/payment-notifications";
    public static final String MBAL_FLEXIBLE_GET_PDF = "/api/v1.1/open/product/ulrp30/processes/%s/quotation/completed-pdf";
    public static final String MBAL_FLEXIBLE_GET_POLICY = "/api/v1/policy";
    public static final String MBAL_FLEXIBLE_UPLOAD_FILES = "/api/v1.1/open/product/ulrp30/processes/%s/files";

    // Call to BAAS Installment
    public static final String EXTERNAL_INSTALLMENT_FEE_CHECK= "/external/installment/fee-check";

    public static final String URL_SAS_URL_DOCUMENT = API + DOWNLOAD + DOCUMENT;

    public static final String MBAL_IC = "/api/v1/sale";

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static final class APP_FLEXIBLE {
        public static final String FLEX_FIND_DETAIL = "/external/flexible/quotation-detail";
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static final class RETRY_MANUAL {
        public static final String REGISTER_AUTO_DEBIT = API + "/retry/register-auto-debit";
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static final class BACKOFFICE {
        public static final String IMPORT_RM_INFO = API + "/backoffice/import-rm-info";
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class ADDRESS {
        public static final String SEARCH_PROVINCES = "/v1/backoffice/provinces";
        public static final String SEARCH_DISTRICTS = "/v1/backoffice/districts";
        public static final String SEARCH_WARDS = "/v1/backoffice/wards";
    }

    // mic flex 3
    public static final String MIC_GET_TOKEN = "/api/e/v1/get-token";
    public static final String MIC_FEE_CARE = "/api/e/sk/v1/phi_miccare";

    public static final String EXTERNAL_INQUIRY_MBAL_POLICY = "/external/policy/mbal/inquiry";

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class MBEMPLOYEE {
        public static final String IMPORT_MB_EMPLOYEES = API+"/v1/import/mb-employee";
        public static final String MB_EMPLOYEES_LIST = API + "/v1/mb-employees";
        public static final String MB_EMPLOYEES_DETAIL = API + "/v1/mb-employee/{mb_id}";
        public static final String MB_EMPLOYEE_HISTORY = API + "/v1/mb-employee/import-history";

    }

}
