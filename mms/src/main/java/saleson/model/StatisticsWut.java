package saleson.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import saleson.common.enumeration.WUTType;
import saleson.common.hibernate.converter.BooleanYnConverter;
import saleson.common.util.DateUtils;
import saleson.model.support.DateAudit;

import javax.persistence.*;

@Data
@Entity
@NoArgsConstructor
public class StatisticsWut extends DateAudit {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long moldId;
    private String moldCode;


//    private String title;
    private Double hourValue;
    private Integer shotCount;
    private String startedAt;
    private String endAt;
    @Enumerated(EnumType.STRING)
    private WUTType wutType;

    private String year;
    private String month;
    private String day;
    private String week;
    private String hour;

    private String yearEnd;
    private String monthEnd;
    private String dayEnd;
    private String weekEnd;
    private String hourEnd;

    @Column(length = 1,columnDefinition = "varchar(1) default 'N'")
    @Convert(converter = BooleanYnConverter.class)
    private boolean valData=false; // check data type

    public StatisticsWut(Long moldId, String moldCode, String title, Double hourValue, Integer shotCount, String startedAt,String endAt,Boolean valData) {
        this.moldId = moldId;
        this.moldCode = moldCode;
//        this.title = title;
        this.hourValue = hourValue;
        this.shotCount = shotCount;
        this.startedAt = startedAt;
        this.wutType = WUTType.getEnumByTitle(title);
        this.year = DateUtils.getYear(startedAt);
        this.month= DateUtils.getYearMonth(startedAt);
        this.day= DateUtils.getDay(startedAt);
        this.hour= DateUtils.getHour(startedAt);
        this.week= DateUtils.getYearWeek(startedAt);
        this.endAt= endAt;
        this.yearEnd = DateUtils.getYear(endAt);
        this.monthEnd = DateUtils.getYearMonth(endAt);
        this.dayEnd = DateUtils.getDay(endAt);
        this.hourEnd = DateUtils.getHour(endAt);
        this.weekEnd = DateUtils.getYearWeek(endAt);
        this.valData = valData;
    }

}
