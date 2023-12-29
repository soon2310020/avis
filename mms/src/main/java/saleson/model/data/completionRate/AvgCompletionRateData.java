package saleson.model.data.completionRate;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;
import saleson.common.enumeration.ObjectType;
import saleson.model.DataRequest;
import saleson.service.util.NumberUtils;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.List;

@Data
@NoArgsConstructor
public class AvgCompletionRateData {
    private ObjectType objectType;
    private double avgRate;
    Page<CompletionRateData> data;

    List<DataRequest> dataRequestList;


    public AvgCompletionRateData(double avgRate, Page<CompletionRateData> data) {
        this.avgRate = NumberUtils.roundToOneDecimalDigit(avgRate);
        this.data = data;
    }

    public AvgCompletionRateData(ObjectType objectType, double avgRate) {
        this.objectType = objectType;
        this.avgRate = avgRate;
    }
}
