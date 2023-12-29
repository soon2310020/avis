package vn.com.twendie.avis.api.model.response;

import lombok.*;
import vn.com.twendie.avis.data.model.Contract;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.util.Objects;

@Data
@EqualsAndHashCode(callSuper = false)
public class VehicleLongPeriodReportModel extends Contract {

    private BigDecimal dailyWorkingTime;
    private BigDecimal contractPeriod;
    private Integer countGroupByPrefix = 1;

    public BigDecimal getDailyWorkingTime() {
        return new BigDecimal((super.getWorkingTimeTo().getTime() - super.getWorkingTimeFrom().getTime()) / 3600000)
                .setScale(1, RoundingMode.HALF_UP);
    }

    public BigDecimal getContractPeriod() {
        return BigDecimal.valueOf(Math.ceil((super.getToDatetime().getTime() - super.getFromDatetime().getTime()) * 1.0 / 2592000000L));
    }
}
