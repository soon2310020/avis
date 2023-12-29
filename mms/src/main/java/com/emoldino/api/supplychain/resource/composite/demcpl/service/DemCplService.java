package com.emoldino.api.supplychain.resource.composite.demcpl.service;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Order;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;

import com.emoldino.api.asset.resource.base.mold.util.MoldUtils;
import com.emoldino.api.common.resource.composite.flt.dto.FltCompany;
import com.emoldino.api.common.resource.composite.flt.dto.FltCompanyIn;
import com.emoldino.api.common.resource.composite.flt.dto.FltIn;
import com.emoldino.api.common.resource.composite.flt.dto.FltPart;
import com.emoldino.api.common.resource.composite.flt.dto.FltPlant;
import com.emoldino.api.common.resource.composite.flt.dto.FltPlantIn;
import com.emoldino.api.common.resource.composite.flt.dto.FltProduct;
import com.emoldino.api.common.resource.composite.flt.dto.FltSupplier;
import com.emoldino.api.common.resource.composite.flt.service.FltService;
import com.emoldino.api.common.resource.composite.flt.util.FltUtils;
import com.emoldino.api.supplychain.resource.base.product.dto.ProductPartDemandByWeeksGetIn;
import com.emoldino.api.supplychain.resource.base.product.dto.ProductPartGetIn;
import com.emoldino.api.supplychain.resource.base.product.dto.ProductPartPlanByWeeksGetIn;
import com.emoldino.api.supplychain.resource.base.product.dto.ProductPartStat;
import com.emoldino.api.supplychain.resource.base.product.dto.ProductPartStatGetIn;
import com.emoldino.api.supplychain.resource.base.product.dto.ProductSupplierGetIn;
import com.emoldino.api.supplychain.resource.base.product.dto.ProductionSummary;
import com.emoldino.api.supplychain.resource.base.product.repository.ProductRepository;
import com.emoldino.api.supplychain.resource.base.product.repository.partstat.PartStatRepository;
import com.emoldino.api.supplychain.resource.base.product.repository.productstat.ProductStat;
import com.emoldino.api.supplychain.resource.base.product.repository.productstat.ProductStatRepository;
import com.emoldino.api.supplychain.resource.base.product.util.ProductUtils;
import com.emoldino.api.supplychain.resource.composite.demcpl.dto.DemCplDetailsGetIn;
import com.emoldino.api.supplychain.resource.composite.demcpl.dto.DemCplDetailsGetOut;
import com.emoldino.api.supplychain.resource.composite.demcpl.dto.DemCplDetailsGetOut.DemCplDetails;
import com.emoldino.api.supplychain.resource.composite.demcpl.dto.DemCplGetIn;
import com.emoldino.api.supplychain.resource.composite.demcpl.dto.DemCplGetOut;
import com.emoldino.api.supplychain.resource.composite.demcpl.dto.DemCplGetOut.DemCplChartItem;
import com.emoldino.api.supplychain.resource.composite.demcpl.dto.DemCplGetOut.DemCplItem;
import com.emoldino.api.supplychain.resource.composite.demcpl.dto.DemCplPart;
import com.emoldino.api.supplychain.resource.composite.demcpl.dto.DemCplPartsGetIn;
import com.emoldino.api.supplychain.resource.composite.demcpl.dto.DemCplProduct;
import com.emoldino.api.supplychain.resource.composite.demcpl.dto.DemCplProductsGetIn;
import com.emoldino.api.supplychain.resource.composite.demcpl.enumeration.DemCplViewBy;
import com.emoldino.api.supplychain.resource.composite.demcpl.repository.DemCplRepository;
import com.emoldino.api.supplychain.resource.composite.demcpl.util.DemCplUtils;
import com.emoldino.framework.dto.TimeSetting;
import com.emoldino.framework.enumeration.TimeScale;
import com.emoldino.framework.util.BeanUtils;
import com.emoldino.framework.util.DateUtils2;
import com.emoldino.framework.util.DateUtils2.DatePattern;
import com.emoldino.framework.util.DateUtils2.Zone;
import com.emoldino.framework.util.ReflectionUtils;
import com.emoldino.framework.util.ValueUtils;
import com.querydsl.core.QueryResults;

import saleson.api.part.PartRepository;
import saleson.common.enumeration.PriorityType;
import saleson.model.Part;

@Service
@Transactional
public class DemCplService {

	public Page<DemCplProduct> getProducts(DemCplProductsGetIn input, Pageable pageable) {
		Page<FltProduct> page = BeanUtils.get(FltService.class).getProducts(ValueUtils.map(input, FltIn.class), pageable);

		String week = !ObjectUtils.isEmpty(input.getTimeValue()) ? input.getTimeValue() : DateUtils2.format(DateUtils2.getInstant(), DatePattern.YYYYww, Zone.GMT);
		List<DemCplProduct> list = page.getContent().stream()//
				.map(product -> {
					long demand = ProductUtils.getDemandQty(product.getId(), week);
					long produced;
					{
						ProductStat stat = BeanUtils.get(ProductStatRepository.class).getWeeklyByProduct(week, product.getId());
						produced = stat == null ? 0L : stat.getProducedVal();
					}
					long capacity = ProductUtils.getWeeklyCapa(week, product.getId(), null, null, produced);
					long partCount = BeanUtils.get(ProductRepository.class).countParts(ProductPartGetIn.builder()//
							.productId(product.getId())//
							.producibleOnly(true)//
							.build()//
					);
					ProductionSummary summary = ProductUtils.toSummary(week, demand, produced, capacity);
					if (partCount == 0L) {
						summary.setDemandComplianceRate(0);
						summary.setDemandCompliance(PriorityType.HIGH);
					}
					return new DemCplProduct(product.getId(), product.getName(), summary.getDemandCompliance(), summary.getDemandComplianceRate());
				})//
				.collect(Collectors.toList());

		return new PageImpl<>(list, pageable, page.getTotalElements());
	}

	public Page<DemCplPart> getParts(DemCplPartsGetIn input, Pageable pageable) {
		Page<FltPart> page = BeanUtils.get(FltService.class).getParts(ValueUtils.map(input, FltIn.class), pageable);

		String week = !ObjectUtils.isEmpty(input.getTimeValue()) ? input.getTimeValue() : DateUtils2.format(DateUtils2.getInstant(), DatePattern.YYYYww, Zone.GMT);
		List<DemCplPart> list = page.getContent().stream()//
				.map(part -> {
					long demand = BeanUtils.get(DemCplRepository.class).findPlanQtyByPartAndWeek(part.getId(), week);
					long produced;
					{
						long[] _produced = { 0L };
						BeanUtils.get(PartStatRepository.class).findAllWeeklyByProduct(week, part.getCategoryId(), part.getId(), null, null)//
								.forEach(partStat -> _produced[0] += partStat.getProducedVal());
						produced = _produced[0];
					}
					long capacity = ProductUtils.getWeeklyCapa(week, part.getCategoryId(), part.getId(), null, produced);
					ProductionSummary summary = ProductUtils.toSummary(week, demand, produced, capacity);
					return new DemCplPart(part, summary.getDemandCompliance(), summary.getDemandComplianceRate());
				})//
				.collect(Collectors.toList());

		return new PageImpl<>(list, pageable, page.getTotalElements());
	}

	public DemCplGetOut get(DemCplGetIn input, Sort sort) {
		return DemCplViewBy.PRODUCT.equals(input.getViewBy()) ? getByProduct(input, sort) : getByPart(input, sort);
	}

	private DemCplGetOut getByProduct(DemCplGetIn input, Sort sort) {
		// 1. Validation
		ValueUtils.assertNotEmpty(input.getId(), "product");
		ValueUtils.assertTimeSetting(input, DemCplUtils.TIME_SCALE_SUPPORTED);

		// 2. Get Part Stats Group by Part ID and Time Value
		Map<Long, Map<String, ProductPartStat>> map = ProductUtils.toPartStatMapByFieldAndTime(//
				BeanUtils.get(ProductRepository.class).findAllPartStatsGroupByPartAndDay(//
						ProductPartStatGetIn.builder()//
								.productId(input.getId())//
								.filterCode(input.getFilterCode())//
								.build(), //
						input), //
				"partId", //
				TimeScale.DATE//
		);

		DemCplGetOut output = new DemCplGetOut();
		Map<Object, DemCplItem> items = new TreeMap<>();
		Order order = sort == null || sort.isUnsorted() ? null : sort.iterator().next();
		int i[] = { 0 };
		ProductPartGetIn reqin = ValueUtils.map(input, ProductPartGetIn.class);
		reqin.setProductId(input.getId());
//		reqin.setProducibleOnly(true);
		reqin.setDate(DateUtils2.toDateRange(TimeSetting.builder()//
				.timeScale(TimeScale.WEEK)//
				.timeValue(input.getTimeValue())//
				.build()).getSecond());
		BeanUtils.get(ProductRepository.class).findAllParts(reqin, PageRequest.of(0, 100, sort)).forEach(part -> {
			DemCplItem item = new DemCplItem();
			item.setPartId(part.getId());
			item.setPartName(part.getName());
			item.setPartCode(part.getPartCode());
			item.setSuppliers(getSuppliers(part.getId(), input.getFilterCode()));
			item.setPlants(getPlants(part.getId(), null, input.getFilterCode()));
			item.setMoldCount(MoldUtils.getMoldCount(part.getId(), null, input.getFilterCode()));

			long requiredQty = ValueUtils.toLong(part.getQuantityRequired(), 1L);

			DemCplChartItem chartItem = new DemCplChartItem();
			output.addChartItem(chartItem);
			chartItem.setPartId(part.getId());
			chartItem.setPartName(part.getName());
			chartItem.setPartCode(part.getPartCode());

			String week = input.getTimeValue();

			long demand = BeanUtils.get(ProductRepository.class).findPartDemandQty(ProductPartDemandByWeeksGetIn.builder()//
					.productId(input.getId())//
					.partId(part.getId())//
					.weeks(week == null ? null : Arrays.asList(week))//
					.build()//
			);

			long produced = 0;
			Map<String, ProductPartStat> stats = map.containsKey(part.getId()) ? map.get(part.getId()) : null;
			populateProduced(item, chartItem, requiredQty, stats, input);
			produced = item.getProducedQuantity();

			long capacity = ProductUtils.getWeeklyCapa(week, input.getId(), part.getId(), null, produced);

			ProductionSummary summary = ProductUtils.toSummary(week, demand, produced, capacity);

			item.setRequiredQuantity(requiredQty);
			item.setProductionDemand(summary.getDemand());
			item.setProducedQuantity(summary.getProduced());
			item.setPredictedQuantity(summary.getPredicted());
			item.setProductionCapacity(summary.getCapacity());
			item.setDemandCompliance(summary.getDemandCompliance());
			item.setDemandComplianceRate(summary.getDemandComplianceRate());

			i[0] = add(output, items, item, order, i[0]);
		});

		sort(output, items, order);

		return output;
	}

	private DemCplGetOut getByPart(DemCplGetIn input, Sort sort) {
		// 1. Validation
		ValueUtils.assertNotEmpty(input.getId(), "part");
		ValueUtils.assertTimeSetting(input, DemCplUtils.TIME_SCALE_SUPPORTED);

		// 2. Get Part Stats Group by Part ID and Time Value
		Map<Long, Map<String, ProductPartStat>> map = ProductUtils.toPartStatMapByFieldAndTime(//
				BeanUtils.get(ProductRepository.class).findAllPartStatsGroupBySupplierAndDay(//
						ProductPartStatGetIn.builder()//
								.partId(input.getId())//
								.filterCode(input.getFilterCode())//
								.build(), //
						input), //
				"supplierId", //
				TimeScale.DATE//
		);

		Part part = BeanUtils.get(PartRepository.class).findById(input.getId()).orElse(null);

		DemCplGetOut output = new DemCplGetOut();
		Map<Object, DemCplItem> items = new TreeMap<>();
		Order order = sort == null || sort.isUnsorted() ? null : sort.iterator().next();
		int i[] = { 0 };
		ProductSupplierGetIn reqin = ValueUtils.map(input, ProductSupplierGetIn.class);
		reqin.setProducibleOnly(true);
		reqin.setDate(DateUtils2.toDateRange(TimeSetting.builder()//
				.timeScale(TimeScale.WEEK)//
				.timeValue(input.getTimeValue())//
				.build()).getSecond());
		BeanUtils.get(ProductRepository.class).findAllSuppliersByPart(input.getId(), reqin, PageRequest.of(0, 100, sort)).forEach(supplier -> {
			DemCplItem item = new DemCplItem();
			if (part != null) {
				item.setPartId(part.getId());
				item.setPartName(part.getName());
				item.setPartCode(part.getPartCode());
			}
			item.setSuppliers(Arrays.asList(new FltCompany(supplier)));
			item.setPlants(getPlants(part.getId(), supplier.getId(), input.getFilterCode()));
			item.setMoldCount(MoldUtils.getMoldCount(part.getId(), supplier.getId(), input.getFilterCode()));

			long requiredQty = ValueUtils.toLong(part.getQuantityRequired(), 1L);

			DemCplChartItem chartItem = new DemCplChartItem();
			output.addChartItem(chartItem);
			chartItem.setSupplierId(supplier.getId());
			chartItem.setSupplierName(supplier.getName());
			chartItem.setSupplierCode(supplier.getCompanyCode());

			String week = input.getTimeValue();
			Map<String, ProductPartStat> stats = map.containsKey(supplier.getId()) ? map.get(supplier.getId()) : null;

			long demand = BeanUtils.get(ProductRepository.class).findPartPlanQty(ProductPartPlanByWeeksGetIn.builder()//
					.productId(part.getCategoryId())//
					.partId(input.getId())//
					.supplierId(Arrays.asList(supplier.getId()))//
					.weeks(week == null ? null : Arrays.asList(week))//
					.build()//
			);
			long produced = 0;
			{
				populateProduced(item, chartItem, requiredQty, stats, input);
				produced = item.getProducedQuantity();
			}
			long capacity = ProductUtils.getWeeklyCapa(week, part.getCategoryId(), part.getId(), Arrays.asList(supplier.getId()), produced);
			ProductionSummary summary = ProductUtils.toSummary(week, demand, produced, capacity);

			item.setRequiredQuantity(requiredQty);
			item.setProductionDemand(summary.getDemand());
			item.setProducedQuantity(summary.getProduced());
			item.setPredictedQuantity(summary.getPredicted());
			item.setProductionCapacity(summary.getCapacity());
			item.setDemandCompliance(summary.getDemandCompliance());
			item.setDemandComplianceRate(summary.getDemandComplianceRate());

			i[0] = add(output, items, item, order, i[0]);
		});

		sort(output, items, order);

		return output;
	}

	private static int add(DemCplGetOut output, Map<Object, DemCplItem> items, DemCplItem item, Order order, int i) {
		if (order == null) {
			output.add(item);
			return i;
		}

		String field = order.getProperty();
		if ("suppliers".equals(field)) {
			String prefix = ObjectUtils.isEmpty(item.getSuppliers()) || item.getSuppliers().get(0) == null || item.getSuppliers().get(0).getName() == null ? ""
					: item.getSuppliers().get(0).getName();
			String suffix = ValueUtils.pad(i++ + "_", 5, "left", "0");
			items.put(prefix + suffix, item);
		} else if ("plants".equals(field)) {
			String prefix = ObjectUtils.isEmpty(item.getPlants()) || item.getPlants().get(0) == null || item.getPlants().get(0).getName() == null ? ""
					: item.getPlants().get(0).getName();
			String suffix = ValueUtils.pad(i++ + "_", 5, "left", "0");
			items.put(prefix + suffix, item);
		} else if ("moldCount".equals(field)) {
			String prefix = ValueUtils.pad(item.getMoldCount(), 5, "left", "0");
			String suffix = ValueUtils.pad(i++ + "_", 5, "left", "0");
			items.put(prefix + suffix, item);
		} else if ("productionDemand".equals(field)) {
			String prefix = ValueUtils.pad(item.getProductionDemand(), 8, "left", "0");
			String suffix = ValueUtils.pad(i++ + "_", 5, "left", "0");
			items.put(prefix + suffix, item);
		} else if ("requiredQuantity".equals(field)) {
			String prefix = ValueUtils.pad(item.getRequiredQuantity(), 8, "left", "0");
			String suffix = ValueUtils.pad(i++ + "_", 5, "left", "0");
			items.put(prefix + suffix, item);
		} else if ("producedQuantity".equals(field)) {
			String prefix = ValueUtils.pad(item.getProducedQuantity(), 8, "left", "0");
			String suffix = ValueUtils.pad(i++ + "_", 5, "left", "0");
			items.put(prefix + suffix, item);
		} else if ("predictedQuantity".equals(field)) {
			String prefix = ValueUtils.pad(item.getPredictedQuantity(), 8, "left", "0");
			String suffix = ValueUtils.pad(i++ + "_", 5, "left", "0");
			items.put(prefix + suffix, item);
		} else if ("productionCapacity".equals(field)) {
			String prefix = ValueUtils.pad(item.getProductionCapacity(), 8, "left", "0");
			String suffix = ValueUtils.pad(i++ + "_", 5, "left", "0");
			items.put(prefix + suffix, item);
		} else if ("demandCompliance".equals(field)) {
			String prefix = ValueUtils.pad(ValueUtils.setScale(BigDecimal.valueOf(item.getDemandComplianceRate() + 100d)).toString(), 6, "left", "0");
			String suffix = ValueUtils.pad(i++ + "_", 5, "left", "0");
			items.put(prefix + suffix, item);
		} else {
			output.add(item);
		}

		return i;
	}

	private static void sort(DemCplGetOut output, Map<Object, DemCplItem> items, Order order) {
		if (items.isEmpty()) {
			return;
		}
		items.forEach((k, item) -> output.add(item));
		if (order != null && !order.isAscending()) {
			Collections.reverse(output.getContent());
		}
	}

	private void populateProduced(DemCplItem item, DemCplChartItem chartItem, long requiredQty, Map<String, ProductPartStat> stats, TimeSetting timeSetting) {
		long produced = 0;
		Map<String, Long> values = new LinkedHashMap<>();
		Pair<String, String> range = DateUtils2.toDateRange(timeSetting);
		Instant fromInst = DateUtils2.toInstant(range.getFirst(), DatePattern.yyyyMMdd, Zone.GMT);
		Instant toInst = DateUtils2.toInstant(range.getSecond(), DatePattern.yyyyMMdd, Zone.GMT);
		Instant currentInst = fromInst;
		do {
			String title = DateUtils2.format(currentInst, DatePattern.yyyyMMdd, Zone.GMT);
			long value = stats != null && stats.containsKey(title) ? stats.get(title).getProduced() : 0L;
			produced += value;
			values.put(title, value);
		} while ((currentInst = currentInst.plus(Duration.ofDays(1))).compareTo(toInst) <= 0);

		int i = 0;
		for (String title : values.keySet()) {
			Long value = values.get(title);
			if (requiredQty != 1 && value != null && !value.equals(0L)) {
				value = Math.round(value.doubleValue() / requiredQty);
			}
			++i;
			try {
				int len = title.length();
				ReflectionUtils.getSetter(DemCplChartItem.class, "title" + i).invoke(chartItem, title.substring(len - 4, len - 2) + "/" + title.substring(len - 2, len));
				ReflectionUtils.getSetter(DemCplChartItem.class, "value" + i).invoke(chartItem, value);
			} catch (Exception e) {
				throw ValueUtils.toAe(e, null);
			}
		}
		item.setProducedQuantity(produced);
	}

	private List<FltCompany> getSuppliers(Long partId, String filterCode) {
		FltCompanyIn reqin = new FltCompanyIn();
		if (partId != null) {
			reqin.setPartId(Arrays.asList(partId));
		}
		reqin.setFilterCode(filterCode);
		Page<FltCompany> page = BeanUtils.get(FltService.class).getSuppliers(reqin, PageRequest.of(0, 100));
		return page.getContent();
	}

	private List<FltPlant> getPlants(Long partId, Long supplierId, String filterCode) {
		FltPlantIn reqin = new FltPlantIn();
		if (partId != null) {
			reqin.setPartId(Arrays.asList(partId));
		}
		if (supplierId != null) {
			reqin.setSupplierId(Arrays.asList(supplierId));
		}
		Page<FltPlant> page = BeanUtils.get(FltService.class).getPlants(reqin, PageRequest.of(0, 100));
		return page.getContent();
	}

	public DemCplDetailsGetOut getDetails(DemCplDetailsGetIn input, Sort sort) {
		// 1. Validation
		ValueUtils.assertTimeSetting(input, DemCplUtils.TIME_SCALE_SUPPORTED);
		ValueUtils.assertNotEmpty(input.getPartId(), "part_id");

		String week = !ObjectUtils.isEmpty(input.getTimeValue()) ? input.getTimeValue() : DateUtils2.format(DateUtils2.getInstant(), DatePattern.YYYYww, Zone.GMT);

		List<FltSupplier> suppliers = FltUtils.getSuppliersByIds(input.getSupplierId());
		FltPart part = FltUtils.getPartById(input.getPartId());

		QueryResults<DemCplDetails> results = BeanUtils.get(DemCplRepository.class).findAllDetailsByPartAndSupplier(input, PageRequest.of(0, 1000, sort));
		results.getResults().forEach(item -> {
			long produced;
			{
				long[] _produced = { 0L };
				BeanUtils.get(PartStatRepository.class)//
						.findAllWeeklyByProduct(input.getTimeValue(), part.getCategoryId(), input.getPartId(), input.getSupplierId(), item.getMoldId())//
						.forEach(partStat -> _produced[0] += partStat.getProducedVal());
				produced = _produced[0];
			}
			long capacity = ProductUtils.getWeeklyCapa(week, part.getCategoryId(), part.getId(), input.getSupplierId(), produced);
			item.setProductionCapacity(capacity);
		});

		Order order = sort == null || sort.isUnsorted() ? null : sort.iterator().next();
		String field = order == null ? null : order.getProperty();
		List<DemCplDetails> content;
		if (ObjectUtils.isEmpty(field) || "moldCode".equals(field) || "supplierName".equals(field) || "equipmentStatus".equals(field) || "moldStatus".equals(field)
				|| "utilizationRate".equals(field) || "approvedCycleTime".equals(field) || "averageCycleTime".equals(field)) {
			content = results.getResults();
		} else {
			Map<Object, DemCplDetails> items = new TreeMap<>();
			content = new ArrayList<>();
			int i = 0;
			for (DemCplDetails item : results.getResults()) {
				if ("producedQuantity".equals(field)) {
					String prefix = ValueUtils.pad(item.getProducedQuantity(), 8, "left", "0");
					String suffix = ValueUtils.pad(i++ + "_", 5, "left", "0");
					items.put(prefix + suffix, item);
				} else if ("productionCapacity".equals(field)) {
					String prefix = ValueUtils.pad(item.getProductionCapacity(), 8, "left", "0");
					String suffix = ValueUtils.pad(i++ + "_", 5, "left", "0");
					items.put(prefix + suffix, item);
				} else {
					content.add(item);
				}
			}
			if (!items.isEmpty()) {
				items.forEach((k, item) -> content.add(item));
				if (order != null && !order.isAscending()) {
					Collections.reverse(content);
				}
			}
		}

		return new DemCplDetailsGetOut(content, suppliers, part);
	}

}
