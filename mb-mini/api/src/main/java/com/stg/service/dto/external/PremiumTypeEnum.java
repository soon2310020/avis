package com.stg.service.dto.external;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum PremiumTypeEnum {
    OVERDUE("Phí quá hạn"), //Quá hạn đóng phí(Ngày hiện tại - ngày tái tục<= 60 ngày)
    DUE("Phí tới hạn"), // Đến hạn đóng phí (Ngày tái tục- ngày hiện tại =0)
    NDUE("Kỳ phí sắp tới (Next Due)"), // Sắp đến hạn đóng phí. (Ngày tái tục - ngày hiện tại  <= 60 ngày)
    RENEWAL_OVERDUE("Tạm thời mất hiệu lực"); // Tạm thời mất hiệu lực (Quá hạn tái tục > 60 ngày)

    private String val;
}
