package com.emoldino.api.analysis.resource.composite.prochg.service;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;

import com.emoldino.api.analysis.resource.composite.prochg.dto.ProChgDetails;
import com.emoldino.api.analysis.resource.composite.prochg.dto.ProChgDetailsGetIn;
import com.emoldino.api.analysis.resource.composite.prochg.dto.ProChgDetailsGetOut;
import com.emoldino.api.analysis.resource.composite.prochg.dto.ProChgGetIn;
import com.emoldino.api.analysis.resource.composite.prochg.dto.ProChgGetOut;
import com.emoldino.api.analysis.resource.composite.prochg.dto.ProChgGetOut.ProChgChartItem;
import com.emoldino.api.analysis.resource.composite.prochg.dto.ProChgItem;
import com.emoldino.api.analysis.resource.composite.prochg.repository.ProChgRepository;
import com.emoldino.api.analysis.resource.composite.prochg.util.ProChgUtils;
import com.emoldino.api.common.resource.base.masterdata.repository.mold2.Mold2;
import com.emoldino.api.common.resource.base.masterdata.repository.mold2.Mold2Repository;
import com.emoldino.api.common.resource.composite.flt.dto.FltPart;
import com.emoldino.api.common.resource.composite.flt.dto.FltPartIn;
import com.emoldino.api.common.resource.composite.flt.service.FltService;
import com.emoldino.framework.dto.TimeSetting;
import com.emoldino.framework.repository.Q;
import com.emoldino.framework.util.BeanUtils;
import com.emoldino.framework.util.DateUtils2;
import com.emoldino.framework.util.DateUtils2.DatePattern;
import com.emoldino.framework.util.DateUtils2.Zone;
import com.emoldino.framework.util.LogicUtils;
import com.emoldino.framework.util.ValueUtils;
import com.querydsl.core.BooleanBuilder;

import lombok.RequiredArgsConstructor;
import saleson.api.statistics.StatisticsPartRepository;
import saleson.api.statistics.StatisticsRepository;

@Service
@Transactional
@RequiredArgsConstructor
public class ProChgService {

	private final ProChgRepository repo;

	public ProChgGetOut get(ProChgGetIn input, TimeSetting timeSetting, Pageable pageable) {
		ValueUtils.assertTimeSetting(timeSetting, ProChgUtils.TIME_SCALE_SUPPORTED);

		Map<String, ProChgChartItem> chartItems = new LinkedHashMap<>();
		if (ProChgUtils.BY_MONTH_SCALES.contains(timeSetting.getTimeScale())) {
			DateUtils2.toMonths(timeSetting).forEach(month -> chartItems.put(month, ProChgChartItem.builder().title(month.substring(4)).build()));
		} else if (ProChgUtils.BY_DAY_SCALES.contains(timeSetting.getTimeScale())) {
			DateUtils2.toDays(timeSetting).forEach(day -> chartItems.put(day, ProChgChartItem.builder().title(day.substring(4, 6) + "/" + day.substring(6)).build()));
		} else {
			return new ProChgGetOut(Collections.emptyList());
		}

		Page<ProChgItem> page = repo.findAll(input, timeSetting, pageable);
		page.getContent().forEach(item -> {
			Instant inst = DateUtils2.toInstant(item.getDateHourRange(), DatePattern.yyyyMMddHH, Zone.GMT);
			item.setDateHourRange(new StringBuilder()//
					.append(DateUtils2.format(inst, DatePattern.yyyy_MM_dd_HH_mm, Zone.GMT)).append(" - ")//
					.append(DateUtils2.format(inst.plus(Duration.ofHours(1L)), "HH:mm", Zone.GMT)).toString());
		});
		ProChgGetOut output = new ProChgGetOut(page);

		output.setTotalProcChgCount(repo.countTotal(input, timeSetting));

		if (input.getMoldId() == null) {
			output.setTopItems(repo.findAllTopItems(input, timeSetting));
		} else {
			ProChgGetIn reqin = ValueUtils.map(input, ProChgGetIn.class);
			reqin.setMoldId(null);
			output.setTopItems(repo.findAllTopItems(reqin, timeSetting));
			output.getTopItems().stream().filter(item -> !item.getMoldId().equals(input.getMoldId())).forEach(item -> item.setProcChgCount(0L));
		}

		ProChgGetIn reqin;
		if (ObjectUtils.isEmpty(input.getMonth()) && ObjectUtils.isEmpty(input.getDay())) {
			reqin = input;
		} else {
			reqin = ValueUtils.map(input, ProChgGetIn.class);
			reqin.setMonth(null);
			reqin.setDay(null);
		}
		repo.findAllProcChgCountItems(reqin, timeSetting).forEach(item -> {
			if (ObjectUtils.isEmpty(item.getTitle())) {
				return;
			}
			if (chartItems.containsKey(item.getTitle())) {
				ProChgChartItem chartItem = chartItems.get(item.getTitle());
				chartItem.setProcChgCount(item.getProcChgCount());
			}
		});
		repo.findAllProdQtyItems(reqin, timeSetting).forEach(item -> {
			if (ObjectUtils.isEmpty(item.getTitle())) {
				return;
			}
			if (chartItems.containsKey(item.getTitle())) {
				ProChgChartItem chartItem = chartItems.get(item.getTitle());
				chartItem.setProdQty(item.getProdQty());
			}
		});

		output.setChartItems(new ArrayList<>(chartItems.values()));

		return output;
	}

	public ProChgDetailsGetOut getDetails(ProChgDetailsGetIn input, Pageable pageable) {
		LogicUtils.assertNotNull(input.getMoldId(), "moldId");
		LogicUtils.assertNotEmpty(input.getDateHourRange(), "dateHourRange");

		Mold2 mold = BeanUtils.get(Mold2Repository.class).findById(input.getMoldId()).orElse(null);

		String hour = ProChgUtils.toHour(input.getDateHourRange());

		// TODO
		BooleanBuilder filter = new BooleanBuilder()//
				.and(Q.statistics.moldId.eq(input.getMoldId()))//
				.and(Q.statistics.year.loe(hour.substring(0, 4)))//
				.and(Q.statistics.day.loe(hour.substring(0, 8)))//
				.and(Q.statistics.hour.loe(hour))//
		;
		List<FltPart> parts = new ArrayList<>();
		BeanUtils.get(StatisticsRepository.class).findAll(filter, PageRequest.of(0, 1, Direction.DESC, "hour")).forEach(stat -> {
			BeanUtils.get(StatisticsPartRepository.class).findByStatisticsId(stat.getId()).forEach(statPart -> {
				parts.addAll(BeanUtils.get(FltService.class)//
						.getParts(FltPartIn.builder().id(statPart.getPartId()).build(), PageRequest.of(0, 50))//
						.getContent());
			});
		});
		if (parts.isEmpty()) {
			parts.addAll(BeanUtils.get(FltService.class)//
					.getParts(FltPartIn.builder().filterCode(input.getFilterCode()).moldId(Collections.singletonList(input.getMoldId())).build(), PageRequest.of(0, 50))//
					.getContent());
		}

		Page<ProChgDetails> page = repo.findAllDetails(input, pageable);
		page.getContent().forEach(item -> {
			item.setProcChgTime(DateUtils2.toOtherPattern(item.getProcChgTime(), DatePattern.yyyyMMddHHmmss, DatePattern.yyyy_MM_dd_HH_mm));
			item.setParts(parts);
		});

		ProChgDetailsGetOut output = new ProChgDetailsGetOut(page);
		output.setDateHourRange(input.getDateHourRange());
		output.setMoldId(input.getMoldId());
		output.setMoldCode(mold == null ? null : mold.getEquipmentCode());
		return output;
	}

//	private ProChgGetOut getMock() {
//		List<ProChgItem> content = Arrays.asList(//
//				ProChgItem.builder()//
//						.moldId(1L).moldCode("T3800001")//
//						.procChgTime("2023-03-14 00:00")//
//						.parts(Arrays.asList(new FltPart(1L, "Part A", "PART_A")))//
//						.supplierId(1L).supplierName("Supplier A")//
//						.locationId(1L).locationName("Plant A")//
//						.build(), //
//				ProChgItem.builder()//
//						.moldId(1L).moldCode("T3800001")//
//						.procChgTime("2023-03-13 00:00")//
//						.parts(Arrays.asList(new FltPart(1L, "Part A", "PART_A")))//
//						.supplierId(1L).supplierName("Supplier A")//
//						.locationId(1L).locationName("Plant A")//
//						.build(), //
//				ProChgItem.builder()//
//						.moldId(2L).moldCode("T3800002")//
//						.procChgTime("2023-03-13 00:00")//
//						.parts(Arrays.asList(new FltPart(2L, "Part B", "PART_B")))//
//						.supplierId(1L).supplierName("Supplier A")//
//						.locationId(2L).locationName("Plant B")//
//						.build(), //
//				ProChgItem.builder()//
//						.moldId(2L).moldCode("T3800002")//
//						.procChgTime("2023-03-11 00:00")//
//						.parts(Arrays.asList(new FltPart(2L, "Part B", "PART_B")))//
//						.supplierId(1L).supplierName("Supplier A")//
//						.locationId(2L).locationName("Plant B")//
//						.build(), //
//				ProChgItem.builder()//
//						.moldId(2L).moldCode("T3800002")//
//						.procChgTime("2023-03-11 00:00")//
//						.parts(Arrays.asList(new FltPart(3L, "Part C", "PART_C")))//
//						.supplierId(1L).supplierName("Supplier A")//
//						.locationId(3L).locationName("Plant C")//
//						.build(), //
//				ProChgItem.builder()//
//						.moldId(3L).moldCode("T3800003")//
//						.procChgTime("2023-03-11 00:00")//
//						.parts(Arrays.asList(new FltPart(3L, "Part C", "PART_C")))//
//						.supplierId(1L).supplierName("Supplier A")//
//						.locationId(3L).locationName("Plant C")//
//						.build(), //
//				ProChgItem.builder()//
//						.moldId(4L).moldCode("T3800004")//
//						.procChgTime("2023-03-11 00:00")//
//						.parts(Arrays.asList(new FltPart(4L, "Part D", "PART_D")))//F
//						.supplierId(1L).supplierName("Supplier A")//
//						.locationId(4L).locationName("Plant D")//
//						.build(), //
//				ProChgItem.builder()//
//						.moldId(5L).moldCode("T3800005")//
//						.procChgTime("2023-03-11 00:00")//
//						.parts(Arrays.asList(new FltPart(4L, "Part D", "PART_D")))//
//						.supplierId(1L).supplierName("Supplier A")//
//						.locationId(4L).locationName("Plant D")//
//						.build()//
//		);
//		ProChgGetOut output = new ProChgGetOut(content);
//
//		output.setTopItems(Arrays.asList(//
//				ProChgTopItem.builder().moldId(2L).moldCode("T3800002").procChgCount(3L).build(), //
//				ProChgTopItem.builder().moldId(2L).moldCode("T3800001").procChgCount(2L).build(), //
//				ProChgTopItem.builder().moldId(2L).moldCode("T3800003").procChgCount(1L).build(), //
//				ProChgTopItem.builder().moldId(2L).moldCode("T3800004").procChgCount(1L).build(), //
//				ProChgTopItem.builder().moldId(2L).moldCode("T3800005").procChgCount(1L).build()//
//		));
//
//		output.setChartItems(Arrays.asList(//
//				ProChgChartItem.builder().title("03/10").procChgCount(0L).prodQty(1003L).build(), //
//				ProChgChartItem.builder().title("03/11").procChgCount(5L).prodQty(850L).build(), //
//				ProChgChartItem.builder().title("03/12").procChgCount(0L).prodQty(2140L).build(), //
//				ProChgChartItem.builder().title("03/13").procChgCount(3L).prodQty(400L).build(), //
//				ProChgChartItem.builder().title("03/14").procChgCount(1L).prodQty(343L).build(), //
//				ProChgChartItem.builder().title("03/15").procChgCount(0L).prodQty(1203L).build(), //
//				ProChgChartItem.builder().title("03/16").procChgCount(0L).prodQty(10L).build()//
//		));
//
//		return output;
//	}

}
