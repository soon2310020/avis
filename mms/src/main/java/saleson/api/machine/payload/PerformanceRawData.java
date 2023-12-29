package saleson.api.machine.payload;

import com.querydsl.core.annotations.QueryProjection;
import lombok.Data;
import saleson.common.util.StringUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

@Data
public class PerformanceRawData {
    private Integer cavity;
    private Integer shotCount;
    private String cttString;
    private Double ct;

    private Double avgCtt;

    @QueryProjection
    public PerformanceRawData(Integer cavity, Integer shotCount, String cttString, Double ct) {
        this.cavity = cavity;
        this.shotCount = shotCount;
        this.cttString = cttString;
        this.ct = ct;


        AtomicReference<Double> totalCtt = new AtomicReference<>((double) 0);
        AtomicInteger totalShot = new AtomicInteger(0);
        if (!StringUtils.isEmpty(cttString)) {
            Map<Double, Integer> cttMap = new HashMap<>(); // key: ctt, value: shot count
            String[] splitCtt = cttString.split("/");
            for (int i = 0; i < splitCtt.length - 1; i += 2) {
                if (StringUtils.isEmpty(splitCtt[i]))
                    break;
                cttMap.put(Double.valueOf(splitCtt[i]), Integer.valueOf(splitCtt[i + 1]));
            }
            if (cttMap.size() > 0) {
                cttMap.forEach((k, v) -> {
                    totalCtt.updateAndGet(v1 -> v1 + Math.abs(k) * v);
                    totalShot.addAndGet(v);
                });
            }
            this.shotCount = totalShot.get();
            this.avgCtt = (double)(totalCtt.get() / 10 / (totalShot.get() == 0 ? 1 : totalShot.get()));
        }
        else
        {
            this.avgCtt = 1D;
        }
    }
}
