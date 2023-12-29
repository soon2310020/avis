package com.emoldino.api.supplychain.resource.composite.parquarsk.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;

import com.emoldino.api.supplychain.resource.composite.parquarsk.dto.ParQuaRskGetIn;
import com.emoldino.api.supplychain.resource.composite.parquarsk.dto.ParQuaRskGetOut;
import com.emoldino.api.supplychain.resource.composite.parquarsk.dto.ParQuaRskGetOut.ParQuaRskChartItem;
import com.emoldino.api.supplychain.resource.composite.parquarsk.dto.ParQuaRskHeatmapItem;
import com.emoldino.api.supplychain.resource.composite.parquarsk.dto.ParQuaRskHeatmapOut;
import com.emoldino.api.supplychain.resource.composite.parquarsk.dto.ParQuaRskItem;
import com.emoldino.api.supplychain.resource.composite.parquarsk.dto.ParQuaRskMold;
import com.emoldino.api.supplychain.resource.composite.parquarsk.repository.ParQuaRskRepository;
import com.emoldino.framework.dto.TimeSetting;
import com.emoldino.framework.enumeration.TimeScale;
import com.emoldino.framework.util.DateUtils2;
import com.emoldino.framework.util.LogicUtils;
import com.emoldino.framework.util.ValueUtils;

import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class ParQuaRskService {
	public static final List<TimeScale> TIME_SCALE_SUPPORTED = Arrays.asList(TimeScale.WEEK);

	private final ParQuaRskRepository repo;

	public ParQuaRskGetOut get(ParQuaRskGetIn input, TimeSetting timeSetting, Pageable pageable) {
		ValueUtils.assertTimeSetting(timeSetting, TIME_SCALE_SUPPORTED);

		Page<ParQuaRskItem> page = repo.findAll(input, timeSetting, pageable);

		page.forEach(item -> {
			if (item.getProdQty() == null || item.getProdQty() == 0 || item.getEstimYieldRate() == null) {
				item.setTrend(ValueUtils.toBigDecimal(0d, 1, null));
				return;
			}
			String week = repo.findPrevWeek(input, timeSetting, item.getMoldId(), item.getPartId(), item.getSupplierId(), item.getLocationId());
			if (ObjectUtils.isEmpty(week)) {
				item.setTrend(ValueUtils.toBigDecimal(0d, 1, null));
				return;
			}
			Double prevEstimYieldRate = repo.findEstimYieldRate(input, week, item.getMoldId(), item.getPartId(), item.getSupplierId(), item.getLocationId());
			item.setTrend(ValueUtils.toBigDecimal(prevEstimYieldRate == null ? 0d : item.getEstimYieldRate().doubleValue() - prevEstimYieldRate.doubleValue(), 1, 0d));
		});

		Map<String, ParQuaRskChartItem> chartItems = new LinkedHashMap<>();
		DateUtils2.toDays(timeSetting).forEach(day -> chartItems.put(day, ParQuaRskChartItem.builder().title(toDateTitle(day)).build()));
		repo.findAllChartItems(input, timeSetting).forEach(item -> {
			ParQuaRskChartItem chartItem = chartItems.get(item.getTimeValue());
			item.setTitle(chartItem.getTitle());
			ValueUtils.map(item, chartItem);
		});

		ParQuaRskItem firstItem = page.isEmpty() ? null : page.getContent().get(0);
		List<ParQuaRskHeatmapItem> heatmapItems = getHeatmapItems(//
				firstItem == null ? null : firstItem.getMoldId(), //
				timeSetting//
		);

		return new ParQuaRskGetOut(page.getContent(), page.getPageable(), page.getTotalElements(), new ArrayList<>(chartItems.values()), //
				firstItem == null ? null : firstItem.getMoldId(), firstItem == null ? null : firstItem.getMoldCode(), heatmapItems);
	}

	public Page<ParQuaRskMold> getMolds(ParQuaRskGetIn input, TimeSetting timeSetting, Pageable pageable) {
		return repo.findAllMolds(input, timeSetting, pageable);
	}

	public ParQuaRskHeatmapOut getHeatmap(Long moldId, TimeSetting timeSetting) {
		LogicUtils.assertNotNull(moldId, "moldId");
		ValueUtils.assertTimeSetting(timeSetting, TIME_SCALE_SUPPORTED);
		return new ParQuaRskHeatmapOut(getHeatmapItems(moldId, timeSetting));
	}

	private List<ParQuaRskHeatmapItem> getHeatmapItems(Long moldId, TimeSetting timeSetting) {
		Map<String, ParQuaRskHeatmapItem> heatmapItems = new LinkedHashMap<>();
		DateUtils2.toDays(timeSetting).forEach(day -> {
			String date = toDateTitle(day);
			for (int i = 0; i < 24; i++) {
				String hour = ValueUtils.pad(i, 2, "left", "0");
				heatmapItems.put(day + hour, ParQuaRskHeatmapItem.builder().date(date).hour(hour).build());
			}
		});

		if (moldId != null) {
			repo.findAllHeatmapItems(moldId, timeSetting).forEach(item -> {
				ParQuaRskHeatmapItem chartItem = heatmapItems.get(item.getTimeValue());
				item.setDate(chartItem.getDate());
				item.setHour(chartItem.getHour());
				ValueUtils.map(item, chartItem);
			});
		}

		return new ArrayList<>(heatmapItems.values());
	}

	private String toDateTitle(String day) {
		return day.substring(4, 6) + "/" + day.substring(6);
	}

}
