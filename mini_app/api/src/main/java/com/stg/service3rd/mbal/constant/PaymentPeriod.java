package com.stg.service3rd.mbal.constant;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PaymentPeriod {
    ANNUAL(1, "Hàng năm"), SEMI_ANNUAL(2, "Nửa năm"), QUARTERLY(4, "Hàng quý"), SINGLE(1, "Hàng năm");

    private final Integer multiple;
    private final String val;

}
