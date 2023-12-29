package com.stg.service3rd.mb_card.adapter;


import com.stg.service3rd.common.adapter.func.IFunction;

public enum MbCardFunctions implements IFunction {
    CONVERT_CARD_NUMB_TO_CARD_ID("/mbcardgw/internet/cardinfo/v1_0/generatetoken"),
    ;

    private final String uri;
    private final boolean hasSaveLog;

    MbCardFunctions(String uri) {
        this.uri = uri;
        this.hasSaveLog = true;
    }

    MbCardFunctions(String uri, boolean hasSaveLog) {
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
