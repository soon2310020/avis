package saleson.model.data.cycleTime;

import com.querydsl.core.annotations.QueryProjection;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import saleson.model.Mold;
import saleson.model.data.ChartData;
import saleson.model.data.PartProductionData;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ToolingCycleTimeData {
	private Long moldId;
	private String moldCode;
	private Mold mold;
	private Integer shotCountCompliance;
	private Double percentageCompliance;
	private Double percentageL1;
	private Double percentageL2;
	private Double cycleTime;
	private Integer shotCount;
	private Double trend;
	private List<ChartData> chartData;

	private Long id;
	private String code;
	private String name;
	private Long numMolds;
	private Long totalMolds;
	private String complianceValue;

	private Integer shotCountAboveL1;
	private Double percentageAboveL1;
	private Integer shotCountBelowL1;
	private Double percentageBelowL1;

	private Integer shotCountAboveL2;
	private Double percentageAboveL2;
	private Integer shotCountBelowL2;
	private Double percentageBelowL2;

	private Double variance;

	private List<ToolingCycleTimeDataLite> details = new ArrayList<>();

	private Integer numberPart;
	private List<PartProductionData> partProductionList;

	@QueryProjection
	public ToolingCycleTimeData(Long moldId, String moldCode, Double cycleTime, Double percentageCompliance, Double percentageL1, Double percentageL2) {
//        this.mold = mold;
		this.moldId = moldId;
		this.moldCode = moldCode;
		this.id = moldId;
		this.code = moldCode;

		this.cycleTime = cycleTime;
		this.percentageCompliance = percentageCompliance;
		this.percentageL1 = percentageL1;
		this.percentageL2 = percentageL2;
	}

	@QueryProjection
	public ToolingCycleTimeData(Long moldId, String moldCode, Double cycleTime, Double percentageCompliance, Double percentageL1, Double percentageL2, Double variance) {
		this.moldId = moldId;
		this.moldCode = moldCode;
		this.id = moldId;
		this.code = moldCode;

		this.cycleTime = cycleTime;
		this.percentageCompliance = percentageCompliance;
		this.percentageL1 = percentageL1;
		this.percentageL2 = percentageL2;

		this.variance = variance;
	}

	@QueryProjection
	public ToolingCycleTimeData(Long moldId, String moldCode, Double cycleTime, Double percentageCompliance, Double percentageL1, Double percentageL2, Integer shotCountAboveL1,
			Double percentageAboveL1, Integer shotCountBelowL1, Double percentageBelowL1, Integer shotCountAboveL2, Double percentageAboveL2, Integer shotCountBelowL2,
			Double percentageBelowL2, Integer shotCountCompliance) {
//        this.mold = mold;
		this.moldId = moldId;
		this.moldCode = moldCode;
		this.id = moldId;
		this.code = moldCode;

		this.cycleTime = cycleTime;
		this.shotCountCompliance = shotCountCompliance;
		this.percentageCompliance = percentageCompliance;
		this.percentageL1 = percentageL1;
		this.percentageL2 = percentageL2;

		this.shotCountAboveL1 = shotCountAboveL1;
		this.percentageAboveL1 = percentageAboveL1;
		this.shotCountBelowL1 = shotCountBelowL1;
		this.percentageBelowL1 = percentageBelowL1;

		this.shotCountAboveL2 = shotCountAboveL2;
		this.percentageAboveL2 = percentageAboveL2;
		this.shotCountBelowL2 = shotCountBelowL2;
		this.percentageBelowL2 = percentageBelowL2;
	}

	@QueryProjection
	public ToolingCycleTimeData(Long moldId, String moldCode, Double cycleTime, Double percentageCompliance, Double percentageL1, Double percentageL2, Integer shotCountAboveL1,
			Double percentageAboveL1, Integer shotCountBelowL1, Double percentageBelowL1, Integer shotCountAboveL2, Double percentageAboveL2, Integer shotCountBelowL2,
			Double percentageBelowL2, Integer shotCountCompliance, Double variance) {
//        this.mold = mold;
		this.moldId = moldId;
		this.moldCode = moldCode;
		this.id = moldId;
		this.code = moldCode;

		this.cycleTime = cycleTime;
		this.shotCountCompliance = shotCountCompliance;
		this.percentageCompliance = percentageCompliance;
		this.percentageL1 = percentageL1;
		this.percentageL2 = percentageL2;

		this.shotCountAboveL1 = shotCountAboveL1;
		this.percentageAboveL1 = percentageAboveL1;
		this.shotCountBelowL1 = shotCountBelowL1;
		this.percentageBelowL1 = percentageBelowL1;

		this.shotCountAboveL2 = shotCountAboveL2;
		this.percentageAboveL2 = percentageAboveL2;
		this.shotCountBelowL2 = shotCountBelowL2;
		this.percentageBelowL2 = percentageBelowL2;

		this.variance = variance;
	}

	@QueryProjection
	public ToolingCycleTimeData(Long moldId, String moldCode, Double cycleTime) {
//        this.mold = mold;
		this.moldId = moldId;
		this.moldCode = moldCode;
		this.id = moldId;
		this.code = moldCode;

		this.cycleTime = cycleTime;
	}

	@QueryProjection
	public ToolingCycleTimeData(Long id, String code, String name, Long moldId, String moldCode, Double cycleTime) {
		this.moldId = moldId;
		this.moldCode = moldCode;
		this.cycleTime = cycleTime;
		this.id = id;
		this.code = code;
		this.name = name;
	}

	@QueryProjection
	public ToolingCycleTimeData(Long moldId, String moldCode, Double cycleTime, Integer shotCountAboveL1, Double percentageAboveL1, Integer shotCountBelowL1,
			Double percentageBelowL1, Integer shotCountAboveL2, Double percentageAboveL2, Integer shotCountBelowL2, Double percentageBelowL2) {
		this.moldId = moldId;
		this.moldCode = moldCode;
		this.id = moldId;
		this.code = moldCode;

		this.cycleTime = cycleTime;

		this.shotCountAboveL1 = shotCountAboveL1;
		this.percentageAboveL1 = percentageAboveL1;
		this.shotCountBelowL1 = shotCountBelowL1;
		this.percentageBelowL1 = percentageBelowL1;

		this.shotCountAboveL2 = shotCountAboveL2;
		this.percentageAboveL2 = percentageAboveL2;
		this.shotCountBelowL2 = shotCountBelowL2;
		this.percentageBelowL2 = percentageBelowL2;
	}

	@QueryProjection
	public ToolingCycleTimeData(Long moldId, String moldCode, Integer shotCount) {
//        this.mold = mold;
		this.moldId = moldId;
		this.moldCode = moldCode;
		this.id = moldId;
		this.code = moldCode;

		this.shotCount = shotCount;
	}

	@QueryProjection
	public ToolingCycleTimeData(Long moldId, Double percentageCompliance) {
		this.moldId = moldId;
		this.id = moldId;

		this.percentageCompliance = percentageCompliance;
	}
}
