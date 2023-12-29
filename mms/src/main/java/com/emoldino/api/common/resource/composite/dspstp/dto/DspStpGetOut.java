package com.emoldino.api.common.resource.composite.dspstp.dto;

import java.util.List;

import com.emoldino.api.common.resource.base.option.dto.ChartTypeConfigItem;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DspStpGetOut {
	private List<ChartTypeConfigItem> chartTypeConfig;
}
