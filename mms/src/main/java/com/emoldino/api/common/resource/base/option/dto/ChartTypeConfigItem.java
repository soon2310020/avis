package com.emoldino.api.common.resource.base.option.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import saleson.common.enumeration.ChartDataConfigType;
import saleson.common.enumeration.ChartType;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChartTypeConfigItem {
	private ChartDataConfigType chartDataType;
	private ChartType chartType;

	public String getChartDataTypeName() {
		return chartDataType == null ? null : chartDataType.getName();
	}

	public ChartTypeConfigItem(ChartDataConfigType chartDataType) {
		this.chartDataType = chartDataType;
		this.chartType = chartDataType.getDefaultType();
	}
}
