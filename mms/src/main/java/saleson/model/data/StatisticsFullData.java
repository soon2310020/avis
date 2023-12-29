package saleson.model.data;

import lombok.Data;
import saleson.model.Statistics;

@Data
public class StatisticsFullData{
    private Long id;

    private Long moldId;
    private String moldCode;

    private String year;
    private String firstShotDateTime;
    private String lastShotDateTime;

    private Integer shotCount;
    private Integer cavity;

    private Double cycleTimeSeconds;
    private Double uptimeMinutes;
    private Integer temperature;

    public StatisticsFullData(Statistics statistics){
        this.id = statistics.getId();
        this.moldId = statistics.getMoldId();
        this.moldCode = statistics.getMoldCode();
        this.year = statistics.getYear();
        this.firstShotDateTime = statistics.getFirstShotDateTime();
        this.lastShotDateTime = statistics.getLastShotDateTime();
        this.shotCount = statistics.getShotCount();
        this.cycleTimeSeconds = statistics.getCycleTimeSeconds();
        this.uptimeMinutes = statistics.getUptimeMinutes();
        this.temperature = statistics.getTav();
    }
}
