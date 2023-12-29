package com.stg.service3rd.crm.adapter;

import com.stg.service3rd.common.adapter.func.IFunction;

public enum CrmFunctions implements IFunction {
    GET_ACCESS_TOKEN("/ms/crmcore-authorization/v1.0/token/serviceAccount"),
    GET_USER_INFO("/ms/crmcore-authorization/v1.0/keycloak/userinfo"),

    GET_CRM_BY_CODE("/ms/crmcore-rm-profile/v1.0/employees/short/{rmCode}"),
    GET_CRM_BY_USERNAME("/ms/crmcore-rm-profile/v1.0/employees/username/{username}?externalData=true"),
    ;

    private final String uri;
    private final boolean hasSaveLog;

    CrmFunctions(String uri) {
        this.uri = uri;
        this.hasSaveLog = true;
    }

    CrmFunctions(String uri, boolean hasSaveLog) {
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
