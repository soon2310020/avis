package com.stg.service3rd.baas.adapter;


import com.stg.service3rd.common.adapter.func.IFunction;

//https://api-private.mbbank.com.vn/private/canary/ms/funds-partner/bulk-cod/v1.0/unregister
//https://api-private.mbbank.com.vn/private/canary/ms/funds-partner/bulk-cod/v1.0/register
public enum BaasFunctions implements IFunction {
    GET_ACCESS_TOKEN("/oauth2/v1/token"),
    REGISTER_AUTO_DEBIT_NON_OTP("/ms/funds-partner/bulk-cod/v1.0/register"), /* Đăng ký thu hộ non-otp */
    COLLECT_BULK_PAYMENTS("/ms/funds-partner/v1.0/bulkcod/payment"), /* Thu hộ theo lô */
    SEARCH_BULK_PAYMENT_INFO("/ms/funds-partner/bulk-cod/v1.0/result"), /* Truy vấn thông tin lô */
    ;

    private final String uri;
    private final boolean hasSaveLog;

    BaasFunctions(String uri) {
        this.uri = uri;
        this.hasSaveLog = true;
    }

    BaasFunctions(String uri, boolean hasSaveLog) {
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
