package com.stg.service3rd.mbalv2.adapter;

import com.stg.service3rd.common.adapter.func.IFunction;

public enum MbalV2Functions implements IFunction {
    POLICY_DETAIL_V2("{endpoint}&interface=SIOS_INQUIRY_PAYMENT"),
    ;

    private final String uri;
    private final boolean hasSaveLog;

    MbalV2Functions(String uri) {
        this.uri = uri;
        this.hasSaveLog = true;
    }

    MbalV2Functions(String uri, boolean hasSaveLog) {
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
