package saleson.model.data.dashboard.totalPart;

import lombok.Data;
import org.springframework.data.domain.Page;

@Data
public class TotalPartChartData {
    private Long level1;
    private Long level2;
    private Page<ProductData> data;
}
