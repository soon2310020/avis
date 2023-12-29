package saleson.model.data;

import com.querydsl.core.annotations.QueryProjection;
import lombok.Data;

@Data
public class CountLocationMold {
    private String countryCode;
    private Long moldCount;

    @QueryProjection
    public CountLocationMold(String countryCode, Long moldCount) {
        this.countryCode = countryCode;
        this.moldCount = moldCount;
    }
}
