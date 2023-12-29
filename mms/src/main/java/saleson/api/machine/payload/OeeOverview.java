package saleson.api.machine.payload;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import saleson.common.enumeration.Comparison;
import saleson.service.util.NumberUtils;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OeeOverview {
    private Long machineCount;

    private Double machineDowntimePercentage;
    private Double machineDowntimeValue;

    private Double partProducedPercentage;
    private Long partProducedValue;

    private Double rejectRatePercentage;
    private Double rejectRateValue;

    private Double availability;
    private Double performance;
    private Double quality;

    public Double getOee() {
        double fa = availability != null ? availability : 0;
        double fq = quality != null ? quality : 0;
        double fp = performance != null ? (performance > 100 ? 100 : performance) : 0;
        double oee = (fa/100) * (fp/100) * (fq/100) * 100;
        return NumberUtils.roundToOneDecimalDigit(oee);
    }
}
