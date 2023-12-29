package com.stg.service.dto.dashboard;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class DashboardOverviewDto {

    private long illustrationNumber;

    private long insuranceRequestNumber;

    private String insuranceRequestPercent;

    private long paymentNumber;

    private String paymentPercent;

    private long contractNumber;

    private String contractPercent;



}
