package com.stg.service3rd.common.dto.soap;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public abstract class SoapResponseBody {
    public abstract EResponse getMessage();
}
