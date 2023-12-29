package saleson.api.machine.payload;

import com.querydsl.core.annotations.QueryProjection;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;
import saleson.service.util.NumberUtils;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
public class AvgOEE {
    private double avgOEE = 0;
    private double avgFA = 0;
    private double avgFP = 0;
    private double avgFQ = 0;

    Page<DetailOEE> details;

    @QueryProjection
    public AvgOEE(double avgFA, double avgFP, double avgFQ) {
        this.avgFA = NumberUtils.roundOffNumber(avgFA);
        this.avgFP = NumberUtils.roundOffNumber(avgFP);
        this.avgFQ = NumberUtils.roundOffNumber(avgFQ);

        this.avgOEE = NumberUtils.roundOffNumber(((avgFA / 100) * (avgFP / 100) * (avgFQ / 100)) * 100);
    }
}
