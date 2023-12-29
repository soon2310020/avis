package saleson.api.machine.payload;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import saleson.service.util.NumberUtils;

import java.util.List;

@Data
@NoArgsConstructor @AllArgsConstructor
@Builder
public class OeeDetail {
    private Long machineId;
    private String machineCode;
    private Long totalPartProduced;
    private Long rejectedPart;
    private Long goodProduction;
    private Double fa;
    private Double fp;
    private Double fq;

    private Long numberOfShift;
    private String start;
    private String end;
    List<OeeByShift> shiftData;

    List<OeeByShift> shift1;
    List<OeeByShift> shift2;
    List<OeeByShift> shift3;
    List<OeeByShift> shift4;

    List<ExpectedHourlyProduction> expectedHourlyProduction;

    public Double getOee() {
        double availability = fa == null ? 0 : fa;
        double performance = fp == null ? 0 : (fp > 100 ? 100 : fp);
        double quality = fq == null ? 0 : fq;
        double oee = (availability/100) * (performance/100) * (quality/100) * 100;
        return NumberUtils.roundOffNumber(oee);
    }
}
