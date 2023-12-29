package saleson.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.querydsl.core.annotations.QueryProjection;
import lombok.*;
import saleson.common.util.DateUtils;
import saleson.model.data.MoldEndLifeCycleChartData;
import saleson.model.support.DateAudit;

import javax.persistence.*;
import java.time.Instant;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode(of = "id", callSuper = false)
@Entity
public class StatisticsAccumulatingShot extends DateAudit {
    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "moldId")
    private Mold mold;

    @Column(insertable = false, updatable = false)
    private Long moldId;

    private Integer designedShot;			// Forecasted Max shots
    private Integer accumulatingShot = 0;
    private Integer remainingShot = 0;

    private String year;
    private String month;
    private String day;
    private String week;
    private Boolean workingInDay;

    public StatisticsAccumulatingShot(Mold mold, Integer accumulatingShot, Integer remainingShot, Instant serverDate) {
        this.mold = mold;
        this.moldId = mold.getId();
        this.accumulatingShot = accumulatingShot;
        this.remainingShot = remainingShot;

        String dateTime = DateUtils.getDate(serverDate, DateUtils.DEFAULT_DATE_FORMAT);
        this.year = DateUtils.getYear(dateTime);
        this.month = DateUtils.getYearMonth(dateTime);
        this.week = DateUtils.getYearWeek(dateTime);
        this.day = DateUtils.getDay(dateTime);
    }
    public StatisticsAccumulatingShot(Mold mold, Integer accumulatingShot, Integer remainingShot, String day) {
        this.mold = mold;
        this.moldId = mold.getId();
        this.accumulatingShot = accumulatingShot;
        this.remainingShot = remainingShot;
        String dateTime = day + "000000";
        this.year = DateUtils.getYear(dateTime);
        this.month = DateUtils.getYearMonth(dateTime);
        this.week = DateUtils.getYearWeek(dateTime);
        this.day = day;
    }
    @QueryProjection
    public StatisticsAccumulatingShot(Mold mold, Integer accumulatingShot,Boolean workingInDay){
        this.mold = mold;
        this.accumulatingShot=accumulatingShot;
        this.workingInDay = workingInDay;
    }
    public MoldEndLifeCycleChartData convertMoldEndLifeCycleChartData(){
        return new MoldEndLifeCycleChartData(this.day,this.accumulatingShot,this.remainingShot,this.year,this.month);
    }
    public MoldEndLifeCycleChartData convertMoldEndLifeCycleChartData(Integer designedShotCurrent ){
        Integer remainShot=designedShotCurrent - (this.accumulatingShot!=null?this.accumulatingShot:0);
        return new MoldEndLifeCycleChartData(this.day, this.accumulatingShot, remainShot > 0 ? remainShot : 0, this.year, this.month);
    }
}
