package com.stg.service3rd.mic_sandbox.adapter;

import com.stg.service3rd.common.adapter.func.IFunction;

public enum MicSandboxFunctions implements IFunction {

    MIC_GET_TOKEN ("/api/e/v1/get-token"),
    MIC_FEE_CARE ("/api/e/sk/v1/phi_miccare"),
    MIC_GENERATE_INSURANCE_CERT_SANDBOX ("/api/e/sk/v1/gcn_miccare"),
    MIC_SEARCH_INSURANCE_CERT_SANDBOX ("/api/e/tracuu/v1/id_trans"),
    MIC_SEARCH_CONTRACT_SANDBOX ("/api/e/sk/v1/ttin-hd-bme")
    ;

    private final String uri;
    private final boolean hasSaveLog;

    MicSandboxFunctions(String uri) {
        this.uri = uri;
        this.hasSaveLog = true;
    }

    MicSandboxFunctions(String uri, boolean hasSaveLog) {
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
