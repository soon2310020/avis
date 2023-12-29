package com.stg.utils;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;

@Getter
@RequiredArgsConstructor
public enum PaymentPeriod {
    ANNUAL(1, "Hàng năm", BigDecimal.valueOf(6000000)),
    SEMI_ANNUAL(2, "Nửa năm", BigDecimal.valueOf(3000000)),
    QUARTERLY(4, "Hàng quý", BigDecimal.valueOf(2000000)),
    SINGLE(1, "Hàng năm", BigDecimal.valueOf(6000000));

    private final Integer multiple;
    private final String val;
    private final BigDecimal min;

}
