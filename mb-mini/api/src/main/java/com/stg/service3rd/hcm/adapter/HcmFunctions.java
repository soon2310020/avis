package com.stg.service3rd.hcm.adapter;

import com.stg.service3rd.common.adapter.func.IFunction;

public enum HcmFunctions implements IFunction {
    GET_TOKEN("/ms/hcm-uat-token/auth/realms/internal/protocol/openid-connect/token"),
    GET_EMPLOYEE_LIST("/ms/hcm-uat/v1.0/get-data?personalId={ccid}&codeSystem={system}"),
    ;

    private final String uri;
    private final boolean hasSaveLog;

    HcmFunctions(String uri) {
        this.uri = uri;
        this.hasSaveLog = true;
    }

    HcmFunctions(String uri, boolean hasSaveLog) {
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
