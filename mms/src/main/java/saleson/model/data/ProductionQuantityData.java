package saleson.model.data;

import com.querydsl.core.annotations.QueryProjection;
import lombok.Data;
import saleson.common.enumeration.Frequent;

@Data
public class ProductionQuantityData {
    private Long id;
    private Integer quantity;
    private String title;

    @QueryProjection
    public ProductionQuantityData(Integer quantity, String title){
        this.quantity = quantity;
        this.title = title;
    }

    @QueryProjection
    public ProductionQuantityData(Long id, Integer quantity, String title){
        this.id = id;
        this.quantity = quantity;
        this.title = title;
    }

    public MoldPartYearWeekOrMonth convert(Frequent frequent){
        MoldPartYearWeekOrMonth moldPartYearWeekOrMonth = MoldPartYearWeekOrMonth.builder().build();
        moldPartYearWeekOrMonth.setCount(Long.valueOf(quantity));
        if(this.title != null && this.title.length() >= 4){
            moldPartYearWeekOrMonth.setYear(Integer.valueOf(this.title.substring(0, 4)));
        }
        if(frequent.equals(Frequent.DAILY) || frequent.equals(Frequent.WEEKLY) || frequent.equals(Frequent.MONTHLY)){
            if(this.title != null && this.title.length() >= 6){
                moldPartYearWeekOrMonth.setWeekOrMonth(Integer.valueOf(this.title.substring(4, 6)));
            }
            if(frequent.equals(Frequent.DAILY) && this.title.length() >= 8){
                moldPartYearWeekOrMonth.setDay(Integer.valueOf(this.title.substring(6, 8)));
            }
        }
        return  moldPartYearWeekOrMonth;
    }
}
