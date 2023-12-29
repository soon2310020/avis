package saleson.model.data;

import lombok.Data;

@Data
public class MoldEndLifeCycleChartData {
    private String title;
    private Long dateTime;
    private Integer accumulatingShot = 0;
    private Integer remainingShot = 0;
    private String year;
    private String month;

    public MoldEndLifeCycleChartData(String title, Long dateTime, Integer accumulatingShot, Integer remainingShot) {
        this.title = title;
        this.dateTime = dateTime;
        this.accumulatingShot = accumulatingShot;
        this.remainingShot = remainingShot;
    }

    public MoldEndLifeCycleChartData(String title, Integer accumulatingShot, Integer remainingShot, String year, String month) {
        this.title = title;
        this.accumulatingShot = accumulatingShot;
        this.remainingShot = remainingShot;
        this.year = year;
        this.month = month;
    }
}
