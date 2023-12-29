package saleson.model.data;

import com.querydsl.core.annotations.QueryProjection;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class MoldPartYearWeekOrMonth {
    Long count;
    Integer year;
    Integer weekOrMonth;
    Integer day;

    String title;

    public MoldPartYearWeekOrMonth(Long count, Integer year, Integer weekOrMonth, Integer day){
        this.count = count;
        this.year = year;
        this.weekOrMonth = weekOrMonth;
        this.day = day;
    }

    @QueryProjection
    public MoldPartYearWeekOrMonth(Long count, Integer year, Integer weekOrMonth){
        this.count = count;
        this.year = year;
        this.weekOrMonth = weekOrMonth;
    }

    @QueryProjection
    public MoldPartYearWeekOrMonth(Long count, Integer year){
        this.count = count;
        this.year = year;
    }
}
