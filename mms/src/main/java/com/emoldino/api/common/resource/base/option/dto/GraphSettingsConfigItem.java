package com.emoldino.api.common.resource.base.option.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import saleson.common.enumeration.ChartDataConfigType;
import saleson.common.enumeration.GraphItemType;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(Include.NON_NULL)
public class GraphSettingsConfigItem {
	private ChartDataConfigType chartDataType;
	private GraphItemType graphItemType;
	private Boolean selected;
}
