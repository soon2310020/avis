package saleson.model.data;

import com.querydsl.core.annotations.QueryProjection;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import saleson.service.util.NumberUtils;

import java.time.Instant;

@Data
@NoArgsConstructor
public class MoldReportData {
    private Long moldId;
    private String moldCode;
    private Integer quantityProduced;
    private Long percentage;

    private Long moldCreatedAt;
    private Long value; // uptime or uptime * cycleTime
    private Long aliveTime;

    @QueryProjection
    public MoldReportData(Long moldId, String moldCode, Integer quantityProduced, Instant moldCreatedAt, Long value) {
        this.moldId = moldId;
        this.moldCode = moldCode;
        this.quantityProduced = quantityProduced;
        this.moldCreatedAt = moldCreatedAt.getEpochSecond();
        long current = Instant.now().getEpochSecond();
        long registeredTime = moldCreatedAt.getEpochSecond();
        this.aliveTime = current - registeredTime;
        this.value = value;
    }

    @QueryProjection
    public MoldReportData(Long moldId, String moldCode, Integer quantityProduced, Instant moldCreatedAt, Long value, Long aliveTime, Long percentage) {
        this.moldId = moldId;
        this.moldCode = moldCode;
        this.quantityProduced = quantityProduced;
        this.moldCreatedAt = moldCreatedAt.getEpochSecond();
        long current = Instant.now().getEpochSecond();
        long registeredTime = moldCreatedAt.getEpochSecond();
        this.aliveTime = aliveTime;
        this.value = value;
        this.percentage = percentage;
    }

    @QueryProjection
    public MoldReportData(Long moldId, Integer quantityProduced) {
        this.moldId = moldId;
        this.quantityProduced = quantityProduced;
    }

    public MoldReportData(Object[] objects) {
        int i = 0;
        this.moldId = NumberUtils.convertToLong(objects[i++]);
        this.moldCode = (String) objects[i++];
        this.quantityProduced = NumberUtils.convertToInteger(objects[i++]);
        Instant mCreateAt = NumberUtils.convertToInstant(objects[i++]);
        this.moldCreatedAt = mCreateAt != null ? mCreateAt.getEpochSecond() : null;
        this.aliveTime = NumberUtils.convertToLong(objects[i++]);
        this.value = NumberUtils.convertToLong(objects[i++]);
        this.percentage = NumberUtils.convertToLong(objects[i++]);
        this.percentage = this.percentage != null ? this.percentage : 0;
    }
}
