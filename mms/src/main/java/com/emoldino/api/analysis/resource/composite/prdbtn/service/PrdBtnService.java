package com.emoldino.api.analysis.resource.composite.prdbtn.service;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import com.emoldino.api.analysis.resource.composite.prdbtn.dto.PrdBtnDetailsGetIn;
import com.emoldino.api.analysis.resource.composite.prdbtn.dto.PrdBtnDetailsGetOut;
import com.emoldino.api.analysis.resource.composite.prdbtn.dto.PrdBtnDetailsGetOut.PrdBtnDetails;
import com.emoldino.api.analysis.resource.composite.prdbtn.dto.PrdBtnGetIn;
import com.emoldino.api.analysis.resource.composite.prdbtn.dto.PrdBtnGetOut;
import com.emoldino.api.analysis.resource.composite.prdbtn.dto.PrdBtnGetOut.PrdBtnChartItem;
import com.emoldino.api.analysis.resource.composite.prdbtn.dto.PrdBtnGetOut.PrdBtnItem;
import com.emoldino.api.asset.resource.base.mold.util.MoldUtils;
import com.emoldino.api.common.resource.composite.flt.dto.FltPart;
import com.emoldino.api.common.resource.composite.flt.dto.FltProduct;
import com.emoldino.api.common.resource.composite.flt.dto.FltSupplier;
import com.emoldino.api.common.resource.composite.flt.util.FltUtils;
import com.emoldino.api.supplychain.resource.base.product.dto.ProductPartStat;
import com.emoldino.api.supplychain.resource.base.product.dto.ProductPartStatGetIn;
import com.emoldino.api.supplychain.resource.base.product.repository.ProductRepository;
import com.emoldino.api.supplychain.resource.base.product.repository.partdemand.PartDemand;
import com.emoldino.api.supplychain.resource.base.product.repository.partdemand.PartDemandRepository;
import com.emoldino.api.supplychain.resource.base.product.util.ProductUtils;
import com.emoldino.framework.enumeration.TimeScale;
import com.emoldino.framework.repository.Q;
import com.emoldino.framework.util.BeanUtils;
import com.emoldino.framework.util.DateUtils2;
import com.emoldino.framework.util.DateUtils2.DatePattern;
import com.emoldino.framework.util.DateUtils2.Zone;
import com.emoldino.framework.util.ReflectionUtils;
import com.emoldino.framework.util.ValueUtils;
import com.querydsl.core.BooleanBuilder;

import saleson.api.mold.MoldRepository;
import saleson.api.part.PartRepository;
import saleson.model.Mold;

@Service
public class PrdBtnService {

	private static final List<TimeScale> TIME_SCALE_SUPPORTED = Arrays.asList(TimeScale.WEEK, TimeScale.MONTH, TimeScale.YEAR, TimeScale.CUSTOM);

	public PrdBtnGetOut get(PrdBtnGetIn input, Pageable pageable) {
		// 1. Validation
		ValueUtils.assertTimeSetting(input, TIME_SCALE_SUPPORTED);
		ValueUtils.assertNotEmpty(input.getFromDate(), "fromDate");
		ValueUtils.assertNotEmpty(input.getToDate(), "toDate");

		ValueUtils.assertNotEmpty(input.getProductId(), "productId");

//		return dummyGetPage(input, pageable);

		// 2. Get Part Stats Group by Part ID and Time Value
		Map<Long, Map<String, ProductPartStat>> map = ProductUtils.toPartStatMapByFieldAndTime(//
				BeanUtils.get(ProductRepository.class).findAllPartStatsGroupByPartAndTime(//
						ProductPartStatGetIn.builder()//
								.productId(input.getProductId())//
								.supplierId(input.getSupplierId())//
								.filterCode(input.getFilterCode())//
								.build(), //
						input), //
				"partId", //
				(TimeScale.YEAR.equals(input.getTimeScale()) ? TimeScale.MONTH : TimeScale.DATE)//
		);

		boolean accumulative = ValueUtils.toBoolean(input.getAccumulative(), true);

		PrdBtnGetOut output = new PrdBtnGetOut();

		BeanUtils.get(PartRepository.class).findAllByProduct(input.getProductId(), null, null, PageRequest.of(0, 1000)).forEach(part -> {
			PrdBtnItem item = new PrdBtnItem();
			output.add(item);
			item.setPartId(part.getId());
			item.setPartName(part.getName());
			item.setPartCode(part.getPartCode());
			long requiredQty = ValueUtils.toLong(part.getQuantityRequired(), 1L);
			item.setRequiredQuantity(requiredQty);
			item.setMoldCount(MoldUtils.getMoldCount(part.getId(), null, null));

			PrdBtnChartItem chartItem = new PrdBtnChartItem();
			output.addChartItem(chartItem);
			chartItem.setPartId(part.getId());
			chartItem.setPartName(part.getName());
			chartItem.setPartCode(part.getPartCode());

			Map<String, ProductPartStat> stats = map.containsKey(part.getId()) ? map.get(part.getId()) : null;

			Instant fromInst = DateUtils2.toInstant(input.getFromDate(), DatePattern.yyyyMMdd, Zone.GMT);
			Instant toInst = DateUtils2.toInstant(input.getToDate(), DatePattern.yyyyMMdd, Zone.GMT);
			long days = (toInst.plus(Duration.ofDays(1)).getEpochSecond() - fromInst.getEpochSecond()) / (60 * 60 * 24);

			long dailyCapacity = 0;
			boolean eachCapacity = false;
			if (!ObjectUtils.isEmpty(part.getMoldParts())) {
				if (ObjectUtils.isEmpty(input.getSupplierId())) {
					item.setMoldCount(part.getMoldParts().size());
				} else {
					part.getMoldParts().forEach(moldPart -> {
						Long supplierId = moldPart.getMold().getCompanyId();
						if (supplierId != null && input.getSupplierId().contains(supplierId)) {
							item.setMoldCount(item.getMoldCount() + 1);
						}
					});
				}

				dailyCapacity = ProductUtils.getDailyCapa(input.getFromDate(), input.getProductId(), part.getId(), input.getSupplierId());
				long toDailiyCapacity = ProductUtils.getDailyCapa(input.getToDate(), input.getProductId(), part.getId(), input.getSupplierId());
				eachCapacity = dailyCapacity != toDailiyCapacity;

				if (!eachCapacity) {
					item.setProductionCapacity(dailyCapacity * days);
				}
			}

			Map<String, Long> values = new LinkedHashMap<>();
			Instant currentInst = fromInst;
			String lastWw = null;
			int lastWwCount = 0;
			double lastWwDemand = 0;
			double demand = 0d;
			do {
				String currentDate = DateUtils2.format(currentInst, DatePattern.yyyyMMdd, Zone.GMT);
				String currentWeek = DateUtils2.format(currentInst, DatePattern.YYYYww, Zone.GMT);
				if (currentWeek.equals(lastWw)) {
					if (lastWwDemand > 0) {
						demand = demand - (lastWwDemand / 7 * lastWwCount);
						demand = demand + (lastWwDemand / 7 * ++lastWwCount);
					}
				} else {
					lastWw = currentWeek;
					lastWwCount = 1;
					PartDemand partDemand = BeanUtils.get(PartDemandRepository.class)//
							.findOne(new BooleanBuilder()//
									.and(Q.partDemand.productId.eq(input.getProductId()))//
									.and(Q.partDemand.partId.eq(part.getId()))//
									.and(Q.partDemand.periodType.eq("WEEKLY"))//
									.and(Q.partDemand.periodValue.eq(currentWeek)))//
							.orElse(null);
					lastWwDemand = partDemand == null ? 0L : partDemand.getQuantity();
					demand = demand + (lastWwDemand / 7);
				}

				if (TimeScale.YEAR.equals(input.getTimeScale())) {
					if (currentDate.endsWith("01")) {
						if (eachCapacity) {
							long capacity = ProductUtils.getDailyCapa(currentDate, input.getProductId(), part.getId(), input.getSupplierId());
							item.setProductionCapacity(item.getProductionCapacity() + capacity * 30);
						}
						String title = DateUtils2.format(currentInst, DatePattern.yyyyMM, Zone.GMT);
						Long value = stats != null && stats.containsKey(title) ? stats.get(title).getProduced() : 0L;
						item.setProducedQuantity(item.getProducedQuantity() + value);
						values.put(title, accumulative ? item.getProducedQuantity() : value);
					}

				} else {
					if (eachCapacity) {
						long capacity = ProductUtils.getDailyCapa(currentDate, input.getProductId(), part.getId(), input.getSupplierId());
						item.setProductionCapacity(item.getProductionCapacity() + capacity);
					}
					String title = DateUtils2.format(currentInst, DatePattern.yyyyMMdd, Zone.GMT);
					Long value = stats != null && stats.containsKey(title) ? stats.get(title).getProduced() : 0L;
					item.setProducedQuantity(item.getProducedQuantity() + value);
					values.put(title, accumulative ? item.getProducedQuantity() : value);
				}

			} while ((currentInst = currentInst.plus(Duration.ofDays(1))).compareTo(toInst) <= 0);
			item.setProductionDemand(ValueUtils.toLong(demand, 0L));

			if (requiredQty <= 0L) {
				requiredQty = 1L;
			}

			int i = 0;
			for (String title : values.keySet()) {
				Long value = values.get(title);
				if (value != null && !value.equals(0L)) {
					value = Math.round(value.doubleValue() / requiredQty);
				}
				++i;
				try {
					int len = title.length();
					ReflectionUtils.getSetter(PrdBtnChartItem.class, "title" + i).invoke(chartItem, title.substring(len - 2, len));
					ReflectionUtils.getSetter(PrdBtnChartItem.class, "value" + i).invoke(chartItem, value);
				} catch (Exception e) {
					throw ValueUtils.toAe(e, null);
				}
			}

		});

		return output;
	}

	public PrdBtnDetailsGetOut getDetails(PrdBtnDetailsGetIn input, Pageable pageable) {
		// 1. Validation
		ValueUtils.assertTimeSetting(input, TIME_SCALE_SUPPORTED);
		ValueUtils.assertNotEmpty(input.getFromDate(), "fromDate");
		ValueUtils.assertNotEmpty(input.getToDate(), "toDate");

		ValueUtils.assertNotEmpty(input.getProductId(), "productId");
		ValueUtils.assertNotEmpty(input.getPartId(), "partId");

//		return dummyGetDetailsPageOut(input, pageable);

		List<FltSupplier> suppliers = FltUtils.getSuppliersByIds(input.getSupplierId());
		FltProduct product = FltUtils.getProductById(input.getProductId());
		FltPart part = FltUtils.getPartById(input.getPartId());

		Instant fromInst = DateUtils2.toInstant(input.getFromDate(), DatePattern.yyyyMMdd, Zone.GMT);
		Instant toInst = DateUtils2.toInstant(input.getToDate(), DatePattern.yyyyMMdd, Zone.GMT);
		long days = (toInst.plus(Duration.ofDays(1)).getEpochSecond() - fromInst.getEpochSecond()) / (60 * 60 * 24);

		// TODO By History
		Map<Long, PrdBtnDetails> content = new LinkedHashMap<>();
		Page<Mold> page = BeanUtils.get(MoldRepository.class).findByProject(input.getProductId(), input.getPartId(), input.getSupplierId(), PageRequest.of(0, 5000));
		page.forEach(mold -> {
			PrdBtnDetails details = new PrdBtnDetails();
			details.setMoldId(mold.getId());
			details.setMoldCode(mold.getEquipmentCode());
			details.setMoldStatus(mold.getEquipmentStatus());
			details.setSupplierId(mold.getSupplierCompanyId());
			details.setSupplierName(mold.getSupplierCompanyName());
			details.setSupplierCode(mold.getSupplierCompanyCode());
			details.setUtilizationRate(mold.getUtilizationRate());
			details.setApprovedCycleTime(mold.getContractedCycleTimeSeconds());
			details.setAverageCycleTime(mold.getWeightedAverageCycleTime());
			long dailyCapacity = 0;
			dailyCapacity = ProductUtils.getDailyCapa(mold);
			details.setProductionCapacity(dailyCapacity * days);
			content.put(mold.getId(), details);
		});

		BeanUtils.get(ProductRepository.class).findAllPartStatsGroupByPartAndMold(//
				ProductPartStatGetIn.builder()//
						.productId(input.getProductId())//
						.supplierId(input.getSupplierId())//
						.filterCode(input.getFilterCode())//
						.build(), //
				input)//
				.forEach(partStat -> {
					if (!content.containsKey(partStat.getMoldId())) {
						return;
					}
					PrdBtnDetails details = content.get(partStat.getMoldId());
					details.setProducedQuantity(partStat.getProduced());
				});

		return new PrdBtnDetailsGetOut(new ArrayList<>(content.values()), pageable, page.getTotalElements(), suppliers, product, part);
	}

}
