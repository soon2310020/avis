package saleson.model.data.rejectedPartRate;

import com.querydsl.core.annotations.QueryProjection;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class RejectedPartBreakDownData {
    private String reason;
    private Integer rejectedAmount;
    private Double rejectedRate;

    @QueryProjection
    public RejectedPartBreakDownData(String reason, Integer rejectedAmount){
        this.reason = reason;
        this.rejectedAmount = rejectedAmount;
    }
}
