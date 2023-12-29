package com.stg.service3rd.ocr.adapter;

import com.stg.service3rd.common.adapter.func.IFunction;

public enum OcrFunctions implements IFunction {
    GET_TOKEN("/ms/auth/realms/internal/protocol/openid-connect/token"),
    VERIFY_SESSION("/ocr-rs/coor/ext/v2.1/processes/verify-session"),
    OCR_PROCESS("/ocr-rs/coor/ext/v2.1/processes?mode=SYNC&ocr_runtime=NOW"),
    ;

    private final String uri;
    private final boolean hasSaveLog;

    OcrFunctions(String uri) {
        this.uri = uri;
        this.hasSaveLog = true;
    }

    OcrFunctions(String uri, boolean hasSaveLog) {
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
