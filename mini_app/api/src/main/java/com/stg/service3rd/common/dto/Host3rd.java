package com.stg.service3rd.common.dto;

import lombok.Getter;
import lombok.Setter;

import static com.stg.service3rd.common.Constants.HostParty;

@Getter
@Setter
public class Host3rd {
    private final String domain;
    private final HostParty name;

    public Host3rd(String domain, HostParty hostName) {
        this.domain = domain;
        this.name = hostName;
    }
}
