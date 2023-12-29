package com.stg.service3rd.common.dto;

import lombok.Getter;
import lombok.Setter;

import static com.stg.utils.Constants.HOST_PARTY;

@Getter
@Setter
public class Host3rd {
    private final String domain;
    private final HOST_PARTY name;

    public Host3rd(String domain, HOST_PARTY hostName) {
        this.domain = domain;
        this.name = hostName;
    }
}
