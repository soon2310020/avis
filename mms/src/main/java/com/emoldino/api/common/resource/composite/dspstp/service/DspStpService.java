package com.emoldino.api.common.resource.composite.dspstp.service;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;

import com.emoldino.api.common.resource.base.option.dto.ChartTypeConfigItem;
import com.emoldino.api.common.resource.base.option.service.OptionService;
import com.emoldino.api.common.resource.base.option.util.OptionUtils;
import com.emoldino.api.common.resource.composite.dspstp.dto.DspStpGetOut;
import com.emoldino.api.common.resource.composite.dspstp.dto.DspStpPostIn;
import com.emoldino.framework.util.BeanUtils;
import com.fasterxml.jackson.core.type.TypeReference;

import saleson.common.enumeration.ChartDataConfigType;

@Service
@Transactional
public class DspStpService {

	public DspStpGetOut get() {
		List<ChartTypeConfigItem> chartTypeConfig = OptionUtils.getUserContent("CHART_TYPE", new TypeReference<List<ChartTypeConfigItem>>() {
		});
		if (ObjectUtils.isEmpty(chartTypeConfig)) {
			chartTypeConfig = Arrays.stream(ChartDataConfigType.values())//
					.filter(type -> !type.equals(ChartDataConfigType.ACCELERATION))//
					.map(ChartTypeConfigItem::new)//
					.collect(Collectors.toList());
		}
		return DspStpGetOut.builder()//
				.chartTypeConfig(chartTypeConfig)//
				.build();
	}

	public void post(DspStpPostIn input) {
		List<ChartTypeConfigItem> newChartTypeConfig = input.getChartTypeConfig();
		if (ObjectUtils.isEmpty(newChartTypeConfig)) {
			return;
		}

		List<ChartTypeConfigItem> chartTypeConfig = OptionUtils.getUserContent("CHART_TYPE", new TypeReference<List<ChartTypeConfigItem>>() {
		});

		if (ObjectUtils.isEmpty(chartTypeConfig)) {
			chartTypeConfig = newChartTypeConfig;
		} else {
			Map<ChartDataConfigType, ChartTypeConfigItem> map = newChartTypeConfig.stream().collect(Collectors.toMap(ChartTypeConfigItem::getChartDataType, Function.identity()));
			chartTypeConfig.forEach(data -> {
				if (!map.containsKey(data.getChartDataType())) {
					return;
				}
				ChartTypeConfigItem item = map.remove(data.getChartDataType());
				data.setChartType(item.getChartType());
			});
			for (ChartTypeConfigItem item : map.values()) {
				chartTypeConfig.add(item);
			}
		}

		BeanUtils.get(OptionService.class).saveUserContent("CHART_TYPE", chartTypeConfig);
	}

}
