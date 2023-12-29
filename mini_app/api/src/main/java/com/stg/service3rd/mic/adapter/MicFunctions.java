package com.stg.service3rd.mic.adapter;

import com.stg.service3rd.common.adapter.func.IFunction;

public enum MicFunctions implements IFunction {
    GENERATE_INSURANCE_CERT("/api/GCN_MICCARE"),
    MIC_INSURANCE_FEE_RESULT("/api/gcn_miccare_phi"),

    MIC_SANDBOX_INSURANCE_FEE_RESULT("/api/e/sk/v1/phi_miccare"),
    MIC_SANDBOX_INSURANCE_CERT("/api/e/sk/v1/gcn_miccare"),
    MIC_SANDBOX_GET_TOKEN("/api/e/v1/get-token"),
    MIC_SANDBOX_SEARCH_PARENT_CONTRACT("/api/e/sk/v1/ttin-hd-bme"),
    MIC_SEARCH_CONTRACT_SANDBOX("/api/e/sk/v1/ttin-hd-bme"),
    ;

    private final String uri;
    private final boolean hasSaveLog;

    MicFunctions(String uri) {
        this.uri = uri;
        this.hasSaveLog = true;
    }

    MicFunctions(String uri, boolean hasSaveLog) {
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
