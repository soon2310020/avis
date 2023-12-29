package saleson.model.data.cycleTime;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class CycleTimeOverviewData {
    private Integer complianceMoldCount;
    private Integer limit1MoldCount;
    private Integer limit2MoldCount;
    private List<ToolingCycleTimeData> complianceMolds;
    private List<ToolingCycleTimeData> limit1Molds;
    private List<ToolingCycleTimeData> limit2Molds;
    private Double trend;
    private List<ToolingCycleTimeData> trendMolds;
    private List<ToolingCycleTimeData> top5Tooling;
    private Long totalElements;
    //for new detail
    private Integer belowMoldCount;
    private Integer aboveMoldCount;
    private List<CycleTimeOverviewDetailData> complianceList;
    private List<CycleTimeOverviewDetailData> belowList;
    private List<CycleTimeOverviewDetailData> aboveList;

    private Double complianceMoldCountPercent;
    private Double belowMoldCountPercent;
    private Double aboveMoldCountPercent;

}
