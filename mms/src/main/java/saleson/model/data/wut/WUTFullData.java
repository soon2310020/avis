package saleson.model.data.wut;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class WUTFullData {
    private String title;
    private Double hour;
    private Integer shotCount;
    private String startedAt;
    private String endAt;
}
