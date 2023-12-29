package com.stg.service3rd.common.dto;

import com.stg.service3rd.common.adapter.func.IFunction;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpMethod;
import org.springframework.lang.NonNull;

import static com.stg.utils.Constants.HOST_PARTY;

@Getter
@Setter
@AllArgsConstructor
public class CallerInfo {
    private final HOST_PARTY hostParty;
    private final String url;
    private final HttpMethod httpMethod;
    private final IFunction func;

    public String getMethodName() {
        return httpMethod.name();
    }

    public static CallerInfo of(@NonNull HOST_PARTY hostParty, @NonNull String url, @NonNull HttpMethod httpMethod, @NonNull IFunction func) {
        return new CallerInfo(hostParty, url, httpMethod, func);
    }
}
