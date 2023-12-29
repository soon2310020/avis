package com.stg.service3rd.common.adapter.func;

import com.stg.service3rd.common.utils.ApiUtil;
import org.springframework.lang.NonNull;

import java.util.Map;

public class Function implements IFunction {
    private final String uri;
    private final String name;
    private final boolean hasSaveLog;

    Function(String uri, String name) {
        this.uri = uri;
        this.name = name;
        this.hasSaveLog = true;
    }

    Function(String uri, String name, boolean hasSaveLog) {
        this.uri = uri;
        this.name = name;
        this.hasSaveLog = hasSaveLog;
    }

    @Override
    public String getUri() {
        return uri;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public boolean hasSaveLog() {
        return hasSaveLog;
    }


    public static IFunction map(IFunction func, @NonNull Map<String, ?> pathVariables) {
        return new Function(ApiUtil.map(func.getUri(), pathVariables), func.getName(), func.hasSaveLog());
    }
}
