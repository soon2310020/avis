package saleson.model.data;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MoldStatisticsDate {
    private Long moldId;
    private Long partId;
    private Integer year;
    private Integer month;
    private Integer day;
}
