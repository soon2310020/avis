package com.stg.common;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Endpoints {
    public static final String PUBLIC = "/public";
    public static final String API = "/v1";
    public static final String AUTH = "/auth";
    public static final String SIGN_IN = "/signin";
    public static final String USER = "/users";
    public static final String UPDATE = "/update";
    public static final String DETAIL = "/detail";
    public static final String FEATURE = "/feature";
    public static final String FORGOT_PASSWORD = "/forgotPassword";

    public static final String URL_REFRESH_TOKEN = API + AUTH + "/refresh";

    // sign in
    public static final String URL_SIGN_IN = API + SIGN_IN;
    public static final String URL_FORGOT_PASSWORD = API + FORGOT_PASSWORD;

    // user
    public static final String URL_USER_UPDATE = API + USER + UPDATE;
    public static final String URL_USER_DETAIL = API + USER + DETAIL;
    public static final String URL_USER_LIST = API + USER;

    public static final String URL_FEATURE_USER_UPDATE = API + USER + FEATURE + UPDATE;

    public static final String URL_CRM_VERIFY = API + "/crm/verify";


    /**
     * BACKOFFICE
     */
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static final class BACKOFFICE {
        public static final String GET_OCCUPATIONS = API + "/occupations";

        public static final String URL = API + "/backoffice";
        public static final String SEARCH_CRM = API + "/backoffice/search-crm";
        public static final String SEARCH_IC = API + "/backoffice/search-ic";
        public static final String IMPORT_RM_INFO = API + "/backoffice/import-rm-info";
        public static final String PROCESS_OCR = API + "/backoffice/process-ocr";

        public static final String LIST_IC = API + "/backoffice/branch/ics";
    }

    /**
     * INSURANCE_PACKAGE
     */
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static final class INSURANCE_PACKAGE {
        public static final String URL = API + "/insurance/package";
        public static final String MIC_PACKAGE = API + "/insurance/mic-packages";
        public static final String MIC_CONTRACT = API + "/insurance/mic/contract";
    }

    /**
     * QUOTATION
     */
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static final class QUOTATION {
        public static final String URL = API + "/quotation";

        public static final String CREATE_PROCESS = API + "/quotation/create-process";
        public static final String CONFIRM = API + "/quotation/confirm";
        public static final String UPDATE_AND_GEN_QRCODE = API + "/quotation/update-and-gen-qrcode";

        public static final String LINK_QRCODE_QUOTE_DETAIL = "/flexible/{quotationUid}/view?insuranceType=FLEXIBLE&cid={cardID}&cidType={cardType}";
        public static final String FIND_QUOTE_DETAIL = API + "/quotation/detail";
        public static final String SYNC_DATA = API + "/quotation/sync-data";
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class ADDRESS {
        public static final String SEARCH_PROVINCES = "/v1/backoffice/provinces";
        public static final String SEARCH_DISTRICTS = "/v1/backoffice/districts";
        public static final String SEARCH_WARDS = "/v1/backoffice/wards";
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static final class POTENTIAL_CUSTOMER {
        public static final String FLOW = API + "/potential-customer/{id}/flow";
        public static final String REFER_SALE = API + "/potential-customer/{id}/refer";
        public static final String REFERRED_DETAIL = API + "/potential-customer/referred/{id}";
        public static final String DELETE_REFERRED = API + "/potential-customer/referred/delete";
        public static final String SEARCH_REFERRED = API + "/potential-customer/referred/search";
        public static final String SEARCH = API + "/potential-customer/search";
        public static final String URL = API + "/potential-customer";
        public static final String DELETE = API + "/potential-customer/delete";
        public static final String SEARCH_DIRECT = API + "/potential-customer/direct/search";
        public static final String DELETE_DIRECT = API + "/potential-customer/direct/delete";
        public static final String INIT_DIRECT = API + "/potential-customer/direct/init";
        public static final String DIRECT_DETAIL = API + "/potential-customer/direct/{id}";
        public static final String DIRECT_QUOTATION_EXAMPLE = API + "/potential-customer/direct/{id}/quotation/example";
        public static final String COMBO_SUGGEST = API + "/potential-customer/{id}/combo/suggest";

    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static final class LEAD {
        public static final String SYNC = PUBLIC + "/sale_portal" + API + "/lead/sync";
    }

}
