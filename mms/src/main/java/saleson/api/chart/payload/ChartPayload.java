package saleson.api.chart.payload;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;
import saleson.common.enumeration.ChartDataType;
import saleson.common.enumeration.DateViewType;
import saleson.common.util.StringUtils;

@Data
public class ChartPayload {
	private DateViewType dateViewType;
	private List<ChartDataType> chartDataType;

	private Long moldId;
	private String partCode;
	private Long partId;
	private String moldCode;

	private String year;
	private String month;
	private String date;

	private String startDate;
	private String endDate;

	private List<Long> moldIdList;//for export

	private boolean skipResinCodeChangeData;

	public void setChartDataTypes(String chartDataTypes) {
		this.chartDataType = new ArrayList<>();
		for (String str : StringUtils.tokenizeToStringArray(chartDataTypes, ",")) {
			ChartDataType value = ChartDataType.valueOf(str);
			if (value == null) {
				continue;
			}
			addChartDataType(value);
		}
	}

	public void addChartDataType(ChartDataType chartDataType) {
		if (this.chartDataType == null) {
			this.chartDataType = new ArrayList<>();
		}
		this.chartDataType.add(chartDataType);
	}
}
