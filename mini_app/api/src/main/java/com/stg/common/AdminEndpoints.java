package com.stg.common;

public final class AdminEndpoints {

    public static final String API = "/admin/v1";

    public static final class POTENTIAL_CUSTOMER {
        public static final String SEARCH = API + "/potential-customer/search";
        public static final String EXPORT = API + "/potential-customer/export";
        public static final String DETAIL = API + "/potential-customer/{id}";
        public static final String SEARCH_REFERRED_BY_CUSTOMER = API + "/potential-customer/{id}/refer/search";
        public static final String SEARCH_DIRECT_BY_CUSTOMER = API + "/potential-customer/{id}/direct/search";
        public static final String SEARCH_REFERRED = API + "/potential-customer/referred/search";
        public static final String EXPORT_REFERRED = API + "/potential-customer/referred/export";
        public static final String REFERRED_DETAIL = API + "/potential-customer/referred/{id}";
        public static final String SEARCH_DIRECT = API + "/potential-customer/direct/search";
        public static final String EXPORT_DIRECT = API + "/potential-customer/direct/export";
        public static final String DIRECT_DETAIL = API + "/potential-customer/direct/{id}";
        public static final String DIRECT_SUBMIT = API + "/potential-customer/direct/{id}/submit";

    }

}
