package saleson.model.data.dashboard;

import com.emoldino.api.common.resource.base.option.dto.RefurbishmentConfig;

import lombok.Data;
import lombok.NoArgsConstructor;
import saleson.common.enumeration.ConfigOption;

@Data
@NoArgsConstructor
public class EndOfLifeCycleChartData {
	private Long highPriority = 0L;
	private Long mediumPriority = 0L;

	private ConfigOption configOption;
	private Double highPriorityMonthConfig = 0D;
	private Double mediumPriorityMonthConfig = 0D;
	private Double highPriorityPercentConfig = 0D;
	private Double mediumPriorityPercentConfig = 0D;

	public EndOfLifeCycleChartData(Long highPriority, Long mediumPriority, RefurbishmentConfig config) {
		this.highPriority = highPriority != null ? highPriority : 0;
		this.mediumPriority = mediumPriority != null ? mediumPriority : 0;
		if (config != null) {
			this.configOption = config.getConfigOption();
			this.highPriorityMonthConfig = config.getHmMonths();
			this.mediumPriorityMonthConfig = config.getMlMonths();
			this.highPriorityPercentConfig = config.getMh();
			this.mediumPriorityPercentConfig = config.getLm();
		}
	}
}
