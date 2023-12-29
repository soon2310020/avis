package com.stg.service.dto.dashboard;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class DashboardCustomerListDto {
    private String id;
    private String fullName;
    private String packageName;
    private String category;
    private String mbalFeePaymentTime;
    private String totalFee;
}
