package saleson.model.data.dashboard;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DigitalizationRateData {
    private Double digitalPercent;
    private Double nonDigitalPercent;
}
