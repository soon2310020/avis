package com.emoldino.api.common.resource.composite.dspstp.dto;

import java.util.List;

import com.emoldino.api.common.resource.base.option.dto.ChartTypeConfigItem;

import lombok.Data;

@Data
public class DspStpPostIn {
	private List<ChartTypeConfigItem> chartTypeConfig;
}
