package com.stg.service3rd.common.dto;

import com.stg.service3rd.common.adapter.func.IFunction;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpMethod;
import org.springframework.lang.NonNull;

import static com.stg.service3rd.common.Constants.HostParty;

@Getter
@Setter
@AllArgsConstructor
public class CallerInfo {
    private final HostParty hostParty;
    private final String url;
    private final HttpMethod httpMethod;
    private final IFunction func;

    public String getMethodName() {
        return httpMethod.name();
    }

    public static CallerInfo of(@NonNull HostParty hostParty, @NonNull String url, @NonNull HttpMethod httpMethod, @NonNull IFunction func) {
        return new CallerInfo(hostParty, url, httpMethod, func);
    }
}
