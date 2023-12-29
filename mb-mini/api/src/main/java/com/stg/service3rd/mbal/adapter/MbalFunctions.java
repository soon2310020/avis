package com.stg.service3rd.mbal.adapter;

import com.stg.service3rd.common.adapter.func.IFunction;

public enum MbalFunctions implements IFunction {
    GET_OCCUPATIONS("/api/v1/occupations"),
    GET_IC("/api/v1/sale"),
    FLEX_CREATE_PROCESSES("/api/v1.1/open/product/ulrp30/processes"),
    FLEX_CREATE_QUOTE("/api/v1.1/open/product/ulrp30/processes/{processId}/quotation"),
    FLEX_CONFIRM_QUOTE("/api/v1.1/open/product/ulrp30/processes/{processId}/quotation/confirmation"),
    FLEX_CHECK_TSA("/api/v1/open/product/ul/application-validations"),

    SUBMIT_POTENTIAL_CUSTOMER("/api/v1/open/leads/mb-leads"),

    GEN_QUOTE_ULSP("/api/v1/ulsp/quotation"),
    GEN_QUOTE_ULRP("/api/v1/ulrp/quotation"),
    GEN_QUOTE_ULRP_3_0("/api/v1/ulrp30/quotation"),

    DOWNLOAD_QUOTE_ULSP("/api/v1/ulsp/quotations/completed-pdf"),
    DOWNLOAD_QUOTE_ULRP("/api/v1/ulrp/quotations/completed-pdf"),
    DOWNLOAD_QUOTE_ULRP_3_0("/api/v1/ulrp30/quotations/completed-pdf"),
    DOWNLOAD_QUOTE_ULRP_3_0_PDF("/api/v1.1/open/product/ulrp30/processes/{processId}/quotation/completed-pdf"),
    ;

    private final String uri;
    private final boolean hasSaveLog;

    MbalFunctions(String uri) {
        this.uri = uri;
        this.hasSaveLog = true;
    }

    MbalFunctions(String uri, boolean hasSaveLog) {
        this.uri = uri;
        this.hasSaveLog = hasSaveLog;
    }


    @Override
    public String getUri() {
        return uri;
    }

    @Override
    public String getName() {
        return this.name();
    }

    @Override
    public boolean hasSaveLog() {
        return hasSaveLog;
    }
}
