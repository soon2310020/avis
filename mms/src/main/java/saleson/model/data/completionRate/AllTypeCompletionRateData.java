package saleson.model.data.completionRate;

import lombok.Data;

import java.util.List;

@Data
public class AllTypeCompletionRateData {
    private Double overall;
    List<AvgCompletionRateData> data;
}
