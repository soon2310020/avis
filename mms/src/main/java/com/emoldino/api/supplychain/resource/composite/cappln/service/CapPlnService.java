package com.emoldino.api.supplychain.resource.composite.cappln.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Order;
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
import com.emoldino.api.common.resource.composite.flt.service.FltService;
import com.emoldino.api.supplychain.resource.base.product.dto.ProductDemandByWeeksGetIn;
import com.emoldino.api.supplychain.resource.base.product.dto.ProductPartDemandByWeeksGetIn;
import com.emoldino.api.supplychain.resource.base.product.dto.ProductPartGetIn;
import com.emoldino.api.supplychain.resource.base.product.dto.ProductPartPlanByWeeksGetIn;
import com.emoldino.api.supplychain.resource.base.product.dto.ProductPartStat;
import com.emoldino.api.supplychain.resource.base.product.dto.ProductPartStatGetIn;
import com.emoldino.api.supplychain.resource.base.product.dto.ProductSupplierGetIn;
import com.emoldino.api.supplychain.resource.base.product.repository.ProductRepository;
import com.emoldino.api.supplychain.resource.base.product.util.ProductUtils;
import com.emoldino.api.supplychain.resource.composite.cappln.dto.CapPlnDetailsGetIn;
import com.emoldino.api.supplychain.resource.composite.cappln.dto.CapPlnDetailsGetOut;
import com.emoldino.api.supplychain.resource.composite.cappln.dto.CapPlnDetailsGetOut.CapPlnDetails;
import com.emoldino.api.supplychain.resource.composite.cappln.dto.CapPlnGetIn;
import com.emoldino.api.supplychain.resource.composite.cappln.dto.CapPlnGetOut;
import com.emoldino.api.supplychain.resource.composite.cappln.dto.CapPlnGetOut.CapPlnChartItem;
import com.emoldino.api.supplychain.resource.composite.cappln.dto.CapPlnGetOut.CapPlnItem;
import com.emoldino.api.supplychain.resource.composite.cappln.dto.CapPlnPartsGetIn;
import com.emoldino.api.supplychain.resource.composite.cappln.dto.CapPlnProductsGetIn;
import com.emoldino.api.supplychain.resource.composite.cappln.enumeration.CapPlnViewBy;
import com.emoldino.api.supplychain.resource.composite.cappln.repository.CapPlnRepository;
import com.emoldino.api.supplychain.resource.composite.cappln.util.CapPlnUtils;
import com.emoldino.framework.dto.ListOut;
import com.emoldino.framework.dto.TimeSetting;
import com.emoldino.framework.enumeration.TimeScale;
import com.emoldino.framework.util.BeanUtils;
import com.emoldino.framework.util.DateUtils2;
import com.emoldino.framework.util.ValueUtils;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import saleson.api.part.PartRepository;
import saleson.model.Part;

@Service
@Transactional
public class CapPlnService {
	private static final List<TimeScale> TIME_SCALE_SUPPORTED = Arrays.asList(TimeScale.MONTH, TimeScale.QUARTER, TimeScale.YEAR);

	public Page<FltProduct> getProducts(CapPlnProductsGetIn input, Pageable pageable) {
		Page<FltProduct> page = BeanUtils.get(FltService.class).getProducts(ValueUtils.map(input, FltIn.class), pageable);
		return page;
	}

	public Page<FltPart> getParts(CapPlnPartsGetIn input, Pageable pageable) {
		Page<FltPart> page = BeanUtils.get(FltService.class).getParts(ValueUtils.map(input, FltIn.class), pageable);
		return page;
	}

	public ListOut<CapPlnItem> get(CapPlnGetIn input, Sort sort) {
		return CapPlnViewBy.PRODUCT.equals(input.getViewBy()) ? getByProduct(input, sort) : getByPart(input, sort);
	}

	private ListOut<CapPlnItem> getByProduct(CapPlnGetIn input, Sort sort) {
		// 1. Validation
		ValueUtils.assertNotEmpty(input.getId(), "product");
		ValueUtils.assertTimeSetting(input, TIME_SCALE_SUPPORTED);

		List<String> weeks = CapPlnUtils.toWeeks(input);

		// 2. Get Part Stats Group by Part ID and Time Value
		Map<Long, Map<String, ProductPartStat>> map = ProductUtils.toPartStatMapByFieldAndTime(//
				BeanUtils.get(ProductRepository.class).findAllPartStatsGroupByPartAndDay(//
						ProductPartStatGetIn.builder()//
								.productId(input.getId())//
								.filterCode(input.getFilterCode())//
								.week(weeks)//
								.build(), //
						input), //
				"partId", //
				TimeScale.WEEK//
		);

		String thisWeek = ProductUtils.getThisWeek();
		String thisMonth = DateUtils2.toMonthByWeek(thisWeek);
		String thisTitle = TimeScale.YEAR.equals(input.getTimeScale()) ? thisMonth : thisWeek;

		CapPlnGetOut output = new CapPlnGetOut();

		Map<String, CapPlnChartItem> chartItems = toChartItemMap(input);
		BeanUtils.get(ProductRepository.class).findDemands(ProductDemandByWeeksGetIn.builder()//
				.productId(input.getId())//
				.weeks(weeks)//
				.build()//
		).forEach(dem -> {
			String week = dem.getTimeValue();
			String key = toKey(input.getTimeScale(), week);
			if (chartItems.containsKey(key)) {
				CapPlnChartItem chartItem = chartItems.get(key);
				chartItem.setProdDemand(chartItem.getProdDemand() + dem.getQuantity());
			}
		});

		Map<Object, CapPlnItem> items = new TreeMap<>();
//		Map<Long, Map<String, Long>> partDemands = new LinkedHashMap<>();
		Map<Long, Map<String, PartQty>> partProds = new LinkedHashMap<>();
		Map<Long, Map<String, PartQty>> partCapas = new LinkedHashMap<>();
		Order order = sort == null || sort.isUnsorted() ? null : sort.iterator().next();
		int i[] = { 0 };
		ProductPartGetIn reqin = ValueUtils.map(input, ProductPartGetIn.class);
		reqin.setProductId(input.getId());
//		reqin.setProducibleOnly(true);
		reqin.setDate(DateUtils2.toDateRange(TimeSetting.builder()//
				.timeScale(input.getTimeScale())//
				.timeValue(input.getTimeValue())//
				.build()).getSecond());
		BeanUtils.get(ProductRepository.class).findAllParts(reqin, PageRequest.of(0, 100, sort)).forEach(part -> {
			CapPlnItem item = new CapPlnItem();
			item.setPartId(part.getId());
			item.setPartName(part.getName());
			item.setPartCode(part.getPartCode());
			item.setSuppliers(getSuppliers(part.getId(), input.getFilterCode()));
//			item.setPlants(getPlants(part.getId(), null, input.getFilterCode()));
			item.setMoldCount(MoldUtils.getMoldCount(part.getId(), null, input.getFilterCode()));

			long requiredQty = ValueUtils.toLong(part.getQuantityRequired(), 1L);
//			Map<String, Long> demandMap = new LinkedHashMap<>();
			Map<String, PartQty> prodMap = new LinkedHashMap<>();
			Map<String, PartQty> capaMap = new LinkedHashMap<>();
			if (requiredQty > 0) {
//				partDemands.put(part.getId(), demandMap);
				partProds.put(part.getId(), prodMap);
				partCapas.put(part.getId(), capaMap);
			}

			long demand = BeanUtils.get(ProductRepository.class).findPartDemandQty(ProductPartDemandByWeeksGetIn.builder()//
					.productId(input.getId())//
					.partId(part.getId())//
					.weeks(weeks)//
					.build()//
			);
//			long demand = 0L
//			for (String week : weeks) {
//				long weekDemand = BeanUtils.get(ProductRepository.class).findPartDemandQty(ProductPartDemandByWeeksGetIn.builder()//
//						.productId(input.getId())//
//						.partId(part.getId())//
//						.weeks(Arrays.asList(week))//
//						.build()//
//				);
//				demandMap.put(week, weekDemand);
//				demand += weekDemand;
//			}

			long produced = 0L;
			Map<String, ProductPartStat> stats = map.containsKey(part.getId()) ? map.get(part.getId()) : null;
			if (stats != null) {
				for (String title : stats.keySet()) {
					String week = title;
					if (!weeks.contains(week)) {
						continue;
					}
					ProductPartStat stat = stats.get(title);
					long qty = stat.getProduced();
					produced += qty;
					String key = toKey(input.getTimeScale(), week);
					if (requiredQty > 0 && chartItems.containsKey(key)) {
						PartQty prod;
						if (!prodMap.containsKey(key)) {
							prodMap.put(key, new PartQty(qty, requiredQty));
						} else {
							prod = prodMap.get(key);
							prod.qty += qty;
						}
					}
				}
			}

			long capacity = 0L;
			for (String week : weeks) {
				long qty = ProductUtils.getWeeklyCapa(week, input.getId(), part.getId(), null, null);
				capacity += qty;
				String key = toKey(input.getTimeScale(), week);
				if (requiredQty > 0 && chartItems.containsKey(key)) {
					PartQty capa;
					if (!capaMap.containsKey(key)) {
						capaMap.put(key, new PartQty(qty, requiredQty));
					} else {
						capa = capaMap.get(key);
						capa.qty += qty;
					}
				}
			}

//			item.setRequiredQty(requiredQty);
			item.setProdDemand(demand);
			item.setProdQty(produced);
			item.setProdCapa(capacity);

			i[0] = add(output, items, item, order, i[0]);
		});

		for (Long partId : partProds.keySet()) {
			Map<String, PartQty> prods = partProds.get(partId);
			if (ObjectUtils.isEmpty(prods)) {
				chartItems.values().forEach(chartItem -> chartItem.setProdQty(0L));
				break;
			}
			prods.forEach((title, prod) -> {
				CapPlnChartItem chartItem = chartItems.get(title);
				if (chartItem.getProdQty() != null && chartItem.getProdQty() == 0L) {
					return;
				}
				long qty = prod.getQty();
				if (qty <= 0) {
					chartItem.setProdQty(0L);
					return;
				}
				long prodQty = qty / prod.getRequired();
				chartItem.setProdQty(chartItem.getProdQty() == null ? prodQty : Math.min(prodQty, chartItem.getProdQty()));
			});
		}

		for (Long partId : partCapas.keySet()) {
			Map<String, PartQty> capas = partCapas.get(partId);
			if (ObjectUtils.isEmpty(capas)) {
				chartItems.values().forEach(chartItem -> chartItem.setProdQty(0L));
				continue;
			}
			capas.forEach((title, capa) -> {
				CapPlnChartItem chartItem = chartItems.get(title);
				int compared = thisTitle.compareTo(title);
				boolean predict = compared < 0;
				if (!predict) {
					if (chartItem.getProdCapa() != null && chartItem.getProdCapa() == 0L) {
						return;
					}
				} else {
					if (chartItem.getPredCapa() != null && chartItem.getPredCapa() == 0L) {
						return;
					}
				}
				long qty = capa.getQty();
				if (qty <= 0) {
					if (!predict) {
						chartItem.setProdCapa(0L);
					} else {
						chartItem.setPredCapa(0L);
					}
					if (compared == 0) {
						chartItem.setPredCapa(chartItem.getProdCapa());
					}
					return;
				}
				long prodCapa = qty / capa.getRequired();
				if (!predict) {
					chartItem.setProdCapa(chartItem.getProdCapa() == null ? prodCapa : Math.min(prodCapa, chartItem.getProdCapa()));
				} else {
					chartItem.setPredCapa(chartItem.getPredCapa() == null ? prodCapa : Math.min(prodCapa, chartItem.getPredCapa()));
				}
				if (compared == 0) {
					chartItem.setPredCapa(chartItem.getProdCapa());
				}
			});
		}

		chartItems.values().forEach(chartItem -> {
			if (chartItem.getProdQty() == null) {
				chartItem.setProdQty(0L);
			}
		});

		sort(output, items, order);
		output.setChartItems(new ArrayList<>(chartItems.values()));

		return output;
	}

	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	private static class PartQty {
		private long qty;
		private long required;
	}

	private static String toKey(TimeScale timeScale, String week) {
		String key = TimeScale.YEAR.equals(timeScale) ? DateUtils2.toMonthByWeek(week) : week;
		return key;
	}

	private ListOut<CapPlnItem> getByPart(CapPlnGetIn input, Sort sort) {
		// 1. Validation
		ValueUtils.assertNotEmpty(input.getId(), "part");
		ValueUtils.assertTimeSetting(input, TIME_SCALE_SUPPORTED);
		List<String> weeks = CapPlnUtils.toWeeks(input);

		Part part = BeanUtils.get(PartRepository.class).findById(input.getId()).orElse(null);

		// 2. Get Part Stats Group by Part ID and Time Value
		Map<Long, Map<String, ProductPartStat>> map = ProductUtils.toPartStatMapByFieldAndTime(//
				BeanUtils.get(ProductRepository.class).findAllPartStatsGroupBySupplierAndDay(//
						ProductPartStatGetIn.builder()//
//								.productId(part.getCategoryId())//
								.partId(input.getId())//
								.filterCode(input.getFilterCode())//
								.week(weeks)//
								.build(), //
						input), //
				"supplierId", //
				TimeScale.WEEK//
		);

		String thisWeek = ProductUtils.getThisWeek();
		String thisMonth = DateUtils2.toMonthByWeek(thisWeek);
		String thisTitle = TimeScale.YEAR.equals(input.getTimeScale()) ? thisMonth : thisWeek;

		CapPlnGetOut output = new CapPlnGetOut();

		Map<String, CapPlnChartItem> chartItems = toChartItemMap(input);

		Map<Object, CapPlnItem> items = new TreeMap<>();
		Map<Long, Map<String, PartQty>> partProds = new LinkedHashMap<>();
		Map<Long, Map<String, PartQty>> partCapas = new LinkedHashMap<>();

		List<Long> supplierId = new ArrayList<>();
		Order order = sort == null || sort.isUnsorted() ? null : sort.iterator().next();
		int i[] = { 0 };
		ProductSupplierGetIn reqin = ValueUtils.map(input, ProductSupplierGetIn.class);
		reqin.setProducibleOnly(true);
		reqin.setDate(DateUtils2.toDateRange(TimeSetting.builder()//
				.timeScale(input.getTimeScale())//
				.timeValue(input.getTimeValue())//
				.build()).getSecond());
		BeanUtils.get(ProductRepository.class).findAllSuppliersByPart(input.getId(), reqin, PageRequest.of(0, 100, sort)).forEach(supplier -> {
			CapPlnItem item = new CapPlnItem();
			if (part != null) {
				item.setPartId(part.getId());
				item.setPartName(part.getName());
				item.setPartCode(part.getPartCode());
			}
			supplierId.add(supplier.getId());
			item.setSuppliers(Arrays.asList(new FltCompany(supplier)));
			item.setPlants(getPlants(part.getId(), supplier.getId(), input.getFilterCode()));
			List<Long> moldIds = MoldUtils.getMoldIds(part.getId(), supplier.getId(), input.getFilterCode());
			item.setMoldCount(ValueUtils.toLong(moldIds.size(), 0L));

			long requiredQty = 1L;// ValueUtils.toLong(part.getQuantityRequired(), 1L);
			Map<String, PartQty> prodMap;
			Map<String, PartQty> capaMap;
			if (partProds.containsKey(part.getId())) {
				prodMap = partProds.get(part.getId());
				capaMap = partCapas.get(part.getId());
			} else {
				prodMap = new LinkedHashMap<>();
				capaMap = new LinkedHashMap<>();
				partProds.put(part.getId(), prodMap);
				partCapas.put(part.getId(), capaMap);
			}

//			CapPlnChartItem chartItem = new CapPlnChartItem();
//			output.addChartItem(chartItem);

			Map<String, ProductPartStat> stats = map.containsKey(supplier.getId()) ? map.get(supplier.getId()) : null;

			long demand = BeanUtils.get(ProductRepository.class).findPartPlanQty(ProductPartPlanByWeeksGetIn.builder()//
//					.productId(part.getCategoryId())//
					.partId(part.getId())//
					.supplierId(Arrays.asList(supplier.getId()))//
					.weeks(weeks)//
					.build()//
			);

			long produced = 0L;
			if (stats != null) {
				for (String title : stats.keySet()) {
					String week = title;
					if (!weeks.contains(week)) {
						continue;
					}
					ProductPartStat stat = stats.get(title);
					long qty = stat.getProduced();
					produced += qty;
					String key = toKey(input.getTimeScale(), week);
					if (requiredQty > 0 && chartItems.containsKey(key)) {
						PartQty prod;
						if (!prodMap.containsKey(key)) {
							prodMap.put(key, new PartQty(qty, requiredQty));
						} else {
							prod = prodMap.get(key);
							prod.qty += qty;
						}
//						CapPlnChartItem chartItem = chartItems.get(key);
//						if (qty <= 0) {
//							chartItem.setProdQty(0L);
//						}
//						long prodQty = qty / requiredQty;
//						chartItem.setProdQty(chartItem.getProdQty() == null ? prodQty : Math.min(prodQty, chartItem.getProdQty()));
					}
				}
			}

			long capacity = 0L;
			for (String week : weeks) {
				long qty = moldIds.isEmpty() ? 0L : ProductUtils.getWeeklyCapa(week, /*part.getCategoryId()*/ null, part.getId(), Arrays.asList(supplier.getId()), moldIds, null);
				capacity += qty;
				String key = toKey(input.getTimeScale(), week);
				if (requiredQty > 0 && chartItems.containsKey(key)) {
					PartQty capa;
					if (!capaMap.containsKey(key)) {
						capaMap.put(key, new PartQty(qty, requiredQty));
					} else {
						capa = capaMap.get(key);
						capa.qty += qty;
					}
//					CapPlnChartItem chartItem = chartItems.get(key);
//					if (chartItem.getProdQty() == null) {
//						chartItem.setProdQty(0L);
//					}
//					if (qty <= 0) {
//						chartItem.setProdCapa(0L);
//					}
//					long prodCapa = qty / requiredQty;
//					if (thisWeek.compareTo(week) >= 0) {
//						chartItem.setProdCapa(chartItem.getProdCapa() == null ? prodCapa : Math.min(prodCapa, chartItem.getProdCapa()));
//					} else {
//						chartItem.setPredCapa(chartItem.getPredCapa() == null ? prodCapa : Math.min(prodCapa, chartItem.getPredCapa()));
//					}
				}
			}

//			item.setRequiredQty(requiredQty);
			item.setProdDemand(demand);
			item.setProdQty(produced);
			item.setProdCapa(capacity);

			i[0] = add(output, items, item, order, i[0]);
		});

		if (!supplierId.isEmpty()) {
			BeanUtils.get(ProductRepository.class).findPartPlans(ProductPartPlanByWeeksGetIn.builder()//
//					.productId(part.getCategoryId())//
					.partId(input.getId())//
					.supplierId(supplierId)//
					.weeks(weeks)//
					.build()//
			).forEach(dem -> {
				String week = dem.getTimeValue();
				String key = toKey(input.getTimeScale(), week);
				if (chartItems.containsKey(key)) {
					CapPlnChartItem chartItem = chartItems.get(key);
					chartItem.setProdDemand(chartItem.getProdDemand() + dem.getQuantity());
				}
			});
		}

		for (Long partId : partProds.keySet()) {
			Map<String, PartQty> prods = partProds.get(partId);
			if (ObjectUtils.isEmpty(prods)) {
				chartItems.values().forEach(chartItem -> chartItem.setProdQty(0L));
				break;
			}
			prods.forEach((title, prod) -> {
				CapPlnChartItem chartItem = chartItems.get(title);
				if (chartItem.getProdQty() != null && chartItem.getProdQty() == 0L) {
					return;
				}
				long qty = prod.getQty();
				if (qty <= 0) {
					chartItem.setProdQty(0L);
					return;
				}
				long prodQty = qty / prod.getRequired();
				chartItem.setProdQty(chartItem.getProdQty() == null ? prodQty : Math.min(prodQty, chartItem.getProdQty()));
			});
		}

		for (Long partId : partCapas.keySet()) {
			Map<String, PartQty> capas = partCapas.get(partId);
			if (ObjectUtils.isEmpty(capas)) {
				chartItems.values().forEach(chartItem -> chartItem.setProdQty(0L));
				break;
			}
			capas.forEach((title, capa) -> {
				CapPlnChartItem chartItem = chartItems.get(title);
				int compared = thisTitle.compareTo(title);
				boolean predict = compared < 0;
				if (!predict) {
					if (chartItem.getProdCapa() != null && chartItem.getProdCapa() == 0L) {
						return;
					}
				} else {
					if (chartItem.getPredCapa() != null && chartItem.getPredCapa() == 0L) {
						return;
					}
				}
				long qty = capa.getQty();
				if (qty <= 0) {
					if (!predict) {
						chartItem.setProdCapa(0L);
					} else {
						chartItem.setPredCapa(0L);
					}
					if (compared == 0) {
						chartItem.setPredCapa(chartItem.getProdCapa());
					}
					return;
				}
				long prodCapa = qty / capa.getRequired();
				if (!predict) {
					chartItem.setProdCapa(chartItem.getProdCapa() == null ? prodCapa : Math.min(prodCapa, chartItem.getProdCapa()));
				} else {
					chartItem.setPredCapa(chartItem.getPredCapa() == null ? prodCapa : Math.min(prodCapa, chartItem.getPredCapa()));
				}
				if (compared == 0) {
					chartItem.setPredCapa(chartItem.getProdCapa());
				}
			});
		}

		chartItems.values().forEach(chartItem -> {
			if (chartItem.getProdQty() == null) {
				chartItem.setProdQty(0L);
			}
		});

		sort(output, items, order);
		output.setChartItems(new ArrayList<>(chartItems.values()));

		return output;
	}

	private static Map<String, CapPlnChartItem> toChartItemMap(TimeSetting timeSetting) {
		Map<String, CapPlnChartItem> chartItems = new LinkedHashMap<>();
		if (TimeScale.YEAR.equals(timeSetting.getTimeScale())) {
			DateUtils2.toMonths(timeSetting).forEach(month -> {
				chartItems.put(month, CapPlnChartItem.builder().title(month.substring(4)).timeValue(month).build());
			});
		} else {
			DateUtils2.toWeeks(timeSetting).forEach(week -> {
				chartItems.put(week, CapPlnChartItem.builder().title("W" + week.substring(4)).timeValue(week).build());
			});
		}
		return chartItems;
	}

	private static int add(CapPlnGetOut output, Map<Object, CapPlnItem> items, CapPlnItem item, Order order, int i) {
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
		} else if ("prodDemand".equals(field) || "productionDemand".equals(field)) {
			String prefix = ValueUtils.pad(item.getProdDemand(), 8, "left", "0");
			String suffix = ValueUtils.pad(i++ + "_", 5, "left", "0");
			items.put(prefix + suffix, item);
//		} else if ("requiredQty".equals(field) || "requiredQuantity".equals(field)) {
//			String prefix = ValueUtils.pad(item.getRequiredQty(), 8, "left", "0");
//			String suffix = ValueUtils.pad(i++ + "_", 5, "left", "0");
//			items.put(prefix + suffix, item);
		} else if ("prodQty".equals(field) || "producedQuantity".equals(field)) {
			String prefix = ValueUtils.pad(item.getProdQty(), 8, "left", "0");
			String suffix = ValueUtils.pad(i++ + "_", 5, "left", "0");
			items.put(prefix + suffix, item);
		} else if ("prodCapa".equals(field) || "productionCapacity".equals(field)) {
			String prefix = ValueUtils.pad(item.getProdCapa(), 8, "left", "0");
			String suffix = ValueUtils.pad(i++ + "_", 5, "left", "0");
			items.put(prefix + suffix, item);
		} else {
			output.add(item);
		}

		return i;
	}

	private static void sort(CapPlnGetOut output, Map<Object, CapPlnItem> items, Order order) {
		if (items.isEmpty()) {
			return;
		}
		items.forEach((k, item) -> output.add(item));
		if (order != null && !order.isAscending()) {
			Collections.reverse(output.getContent());
		}
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

	public CapPlnDetailsGetOut getDetails(CapPlnDetailsGetIn input, Sort sort) {
		// 1. Validation
		ValueUtils.assertNotEmpty(input.getPartId(), "part");
		ValueUtils.assertTimeSetting(input, TIME_SCALE_SUPPORTED);
		List<String> weeks = CapPlnUtils.toWeeks(input);

		Part part = BeanUtils.get(PartRepository.class).findById(input.getPartId()).orElse(null);

		List<FltCompany> suppliers;
		if (input.getSupplierId() == null) {
			suppliers = getSuppliers(input.getPartId(), input.getFilterCode());
		} else {
			FltCompanyIn reqin = new FltCompanyIn();
			reqin.setId(input.getSupplierId());
			reqin.setPartId(Arrays.asList(input.getPartId()));
			reqin.setFilterCode(input.getFilterCode());
			Page<FltCompany> page = BeanUtils.get(FltService.class).getSuppliers(reqin, PageRequest.of(0, 100));
			suppliers = page.getContent();
		}
		long demand = BeanUtils.get(ProductRepository.class).findPartPlanQty(ProductPartPlanByWeeksGetIn.builder()//
//				.productId(part.getCategoryId())//
				.partId(part.getId())//
				.supplierId(input.getSupplierId() == null ? null : Arrays.asList(input.getSupplierId()))//
				.weeks(weeks)//
				.build()//
		);

		List<CapPlnDetails> content = BeanUtils.get(CapPlnRepository.class).findAllDetails(input, sort);
		content.forEach(detail -> {
			long prodQty = BeanUtils.get(ProductRepository.class).findProdQty(//
					ProductPartStatGetIn.builder()//
							.filterCode(input.getFilterCode())//
//							.productId(part.getCategoryId())//
							.partId(part.getId())//
							.moldId(detail.getMoldId())//
							.supplierId(input.getSupplierId() == null ? null : Arrays.asList(input.getSupplierId()))//
							.week(weeks)//
							.build(), //
					input);
			detail.setProdQty(prodQty);
			long capa = 0L;
			for (String week : weeks) {
				capa += ProductUtils.getWeeklyCapa(week, /*part.getCategoryId()*/ null, part.getId(), input.getSupplierId() == null ? null : Arrays.asList(input.getSupplierId()),
						Arrays.asList(detail.getMoldId()), null);
			}
			detail.setProdCapa(capa);
		});
		CapPlnDetailsGetOut output = ValueUtils.map(input, CapPlnDetailsGetOut.class);
		if (part != null) {
			output.setPartId(part.getId());
			output.setPartName(part.getName());
			output.setPartCode(part.getPartCode());
		}
		output.setProdDemand(demand);
		output.setSuppliers(suppliers);
		output.setContent(content);
		return output;
	}

}
