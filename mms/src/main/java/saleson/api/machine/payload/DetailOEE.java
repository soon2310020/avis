package saleson.api.machine.payload;

import com.querydsl.core.annotations.QueryProjection;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import saleson.model.MachineStatistics;
import saleson.service.util.NumberUtils;

import java.util.List;

@Data
@NoArgsConstructor
public class DetailOEE {
    private String name;
    private double avgOEE;
    private double avgFA;
    private double avgFP;
    private double avgFQ;

    public DetailOEE(MachineStatistics machineStatistics) {
        this.name = machineStatistics.getMachine().getMachineCode();
        this.avgOEE = NumberUtils.roundOffNumber((machineStatistics.getOee()));
        this.avgFA = NumberUtils.roundOffNumber((machineStatistics.getFa()));
        this.avgFP = NumberUtils.roundOffNumber((machineStatistics.getFp()));
        this.avgFQ = NumberUtils.roundOffNumber((machineStatistics.getFq()));
    }

    @QueryProjection
    public DetailOEE(String name, double avgOEE, double avgFA, double avgFP, double avgFQ) {
        this.name = name;
        this.avgFA = NumberUtils.roundOffNumber(avgFA);
        this.avgFP = NumberUtils.roundOffNumber(avgFP);
        this.avgFQ = NumberUtils.roundOffNumber(avgFQ);

        this.avgOEE = NumberUtils.roundOffNumber(((avgFA / 100) * (avgFP / 100) * (avgFQ / 100)) * 100);
    }
}
