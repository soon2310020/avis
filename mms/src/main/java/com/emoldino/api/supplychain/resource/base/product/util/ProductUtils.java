package com.emoldino.api.supplychain.resource.base.product.util;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.data.domain.PageRequest;
import org.springframework.util.ObjectUtils;

import com.emoldino.api.analysis.resource.base.production.dto.ProdBarChart;
import com.emoldino.api.supplychain.resource.base.product.dto.ProductPartGetIn;
import com.emoldino.api.supplychain.resource.base.product.dto.ProductPartStat;
import com.emoldino.api.supplychain.resource.base.product.dto.ProductionSummary;
import com.emoldino.api.supplychain.resource.base.product.repository.ProductRepository;
import com.emoldino.api.supplychain.resource.base.product.repository.partstat.PartStat;
import com.emoldino.api.supplychain.resource.base.product.repository.productdemand.ProductDemand;
import com.emoldino.api.supplychain.resource.base.product.service.ProductService;
import com.emoldino.framework.dto.TimeSetting;
import com.emoldino.framework.enumeration.TimeScale;
import com.emoldino.framework.exception.LogicException;
import com.emoldino.framework.repository.Q;
import com.emoldino.framework.util.BeanUtils;
import com.emoldino.framework.util.CacheUtils;
import com.emoldino.framework.util.DateUtils2;
import com.emoldino.framework.util.DateUtils2.DatePattern;
import com.emoldino.framework.util.DateUtils2.Zone;
import com.emoldino.framework.util.LogicUtils;
import com.emoldino.framework.util.QueryUtils;
import com.emoldino.framework.util.ValueUtils;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import saleson.api.mold.MoldPartRepository;
import saleson.common.enumeration.PriorityType;
import saleson.model.Mold;
import saleson.model.MoldPart;
import saleson.model.Part;
import saleson.model.QCategory;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ProductUtils {

	public static String getStatSince() {
		return "20220401";
	}

	/*
	 * Demand Methods
	 */

	public static long getDemandQty(Long productId, String week) {
		return BeanUtils.get(ProductService.class).getDemandQty(productId, "WEEKLY", week);
	}

	public static long getDemandQtyByBrand(Long brandId, String week) {
		return BeanUtils.get(ProductService.class).getDemandQtyByBrand(brandId, "WEEKLY", week);
	}

	public static void saveDemand(Long productId, String week, long qty) {
		BeanUtils.get(ProductService.class).saveDemand(new ProductDemand(productId, "WEEKLY", week, qty));
	}

	public static void saveDemand(Long productId, List<String> weeks, long qty) {
		if (productId == null || ObjectUtils.isEmpty(weeks)) {
			return;
		}
		weeks.forEach(week -> saveDemand(productId, week, qty));
	}

	public static long getPartDemandQty(Long productId, Long partId, String periodValue) {
		return BeanUtils.get(ProductService.class).getPartDemandQty(productId, partId, "WEEKLY", periodValue);
	}

	public static long getPartDemandQtyByBrand(Long brandId, Long partId, String periodValue) {
		return BeanUtils.get(ProductService.class).getPartDemandQtyByBrand(brandId, partId, "WEEKLY", periodValue);
	}

	/*
	 * Capacity Methods
	 */

	@Data
	public static class CapaSummary {
		private Map<Long, Mold> molds = new HashMap<>();
		private long totalCapacity;
		private Map<Long, Long> supplierCapacities = new HashMap<>();
	}

	public static CapaSummary getDailyCapaSummary(String day, Long partId) {
		return setDailyCapaSummary(new CapaSummary(), day, partId);
	}

	public static CapaSummary setDailyCapaSummary(CapaSummary summary, String day, Long partId) {
		Map<Long, Mold> molds = summary.getMolds();
		Map<Long, Long> capacities = summary.getSupplierCapacities();

		{
			BooleanBuilder filter = toProducibleMoldPartFilter(partId, day);
			BeanUtils.get(MoldPartRepository.class).findAll(filter).forEach(moldPart -> {
				Mold mold = moldPart.getMold();
				if (molds.containsKey(mold.getId()) //
//						|| !ThreadUtils.getProp("ProductUtils.stats.mold.exists." + mold.getId(),
//								() -> BeanUtils.get(StatisticsRepository.class).exists(Q.statistics.moldId.eq(mold.getId())))//
				) {
					return;
				}

				molds.put(mold.getId(), mold);
				long capacity = getDailyCapa(mold);

				summary.setTotalCapacity(summary.getTotalCapacity() + capacity);
				long prevCapacity = capacities.containsKey(mold.getCompanyId()) ? capacities.get(mold.getCompanyId()) : 0L;
				capacities.put(mold.getCompanyId(), prevCapacity + capacity);
			});
		}
		return summary;
	}

	public static long getDailyCapa(Mold mold) {
		return mold.getMaxCapacityPerWeek() == null || mold.getMaxCapacityPerWeek().equals(0) ? 0 : mold.getMaxCapacityPerWeek() / 7;
	}

	public static long getDailyCapa(String day, Long productId, Long partId, List<Long> supplierId) {
		BooleanBuilder filter = applyProducibleMoldPartFilter(new BooleanBuilder(), null, productId, partId, supplierId, null, day);

		long[] capacity = { 0L };
		BeanUtils.get(MoldPartRepository.class).findAll(filter).forEach(item -> capacity[0] += ProductUtils.getDailyCapa(item.getMold()));
		return capacity[0];
	}

	public static long getWeeklyCapa(String week, Long productId, Long partId, List<Long> supplierId, Long produced) {
		return getWeeklyCapa(week, null, productId, partId, supplierId, null, produced);
	}

	public static long getWeeklyCapa(String week, Long productId, Long partId, List<Long> supplierId, List<Long> moldId, Long produced) {
		return getWeeklyCapa(week, null, productId, partId, supplierId, moldId, produced);
	}

	public static long getWeeklyCapaByBrand(String week, Long brandId, Long partId, Long supplierId, Long produced) {
		return getWeeklyCapa(week, brandId, null, partId, supplierId == null ? null : Arrays.asList(supplierId), null, produced);
	}

	private static long getWeeklyCapa(String week, Long brandId, Long productId, Long partId, List<Long> supplierId, List<Long> moldId, Long produced) {
		ValueUtils.assertNotEmpty(week, "week");

		int compared = week.compareTo(ProductUtils.getThisWeek());

		Long value = null;
		// Past
		if (brandId == null && compared < 0) {
			BooleanBuilder filter = new BooleanBuilder();
			filter.and(Q.prodMoldStat.week.eq(week));
			if (productId != null) {
				filter.and(Q.prodMoldStat.productId.eq(productId));
			}
			if (partId != null) {
				filter.and(Q.prodMoldStat.partId.eq(partId));
			}
			if (ObjectUtils.isEmpty(supplierId)) {
				filter.and(Q.prodMoldStat.supplierId.isNotNull());
			} else {
				filter.and(Q.prodMoldStat.supplierId.in(supplierId));
			}
			if (!ObjectUtils.isEmpty(moldId)) {
				filter.and(Q.prodMoldStat.moldId.in(moldId));
			}

			if (partId != null || !ObjectUtils.isEmpty(supplierId)) {
				value = BeanUtils.get(JPAQueryFactory.class)//
						.select(Q.prodMoldStat.weeklyCapa.sum())//
						.from(Q.prodMoldStat)//
						.where(filter)//
						.fetchOne();
			} else {
				String day = DateUtils2.toDateRange(TimeSetting.builder()//
						.timeScale(TimeScale.WEEK)//
						.timeValue(week)//
						.build()).getSecond();
				Map<Long, Long> capacities = new HashMap<>();
				BeanUtils.get(MoldPartRepository.class)//
						.findAll(applyProducibleMoldPartFilter(new BooleanBuilder(), brandId, productId, partId, supplierId, moldId, day))//
						.forEach(moldPart -> {
							long capacity = ValueUtils.toLong(capacities.get(moldPart.getPartId()), 0L);
							capacity += getWeeklyCapa(week, brandId, productId, moldPart.getId(), supplierId, moldId, null);
							capacities.put(moldPart.getPartId(), capacity);
						});
				value = min(capacities);
			}
		}

		if (value == null) {
			String day = DateUtils2.toDateRange(TimeSetting.builder()//
					.timeScale(TimeScale.WEEK)//
					.timeValue(week)//
					.build()).getSecond();

			if (partId != null || !ObjectUtils.isEmpty(supplierId)) {
				long[] capacity = { 0L };
				BeanUtils.get(MoldPartRepository.class)//
						.findAll(applyProducibleMoldPartFilter(new BooleanBuilder(), brandId, productId, partId, supplierId, moldId, day))//
						.forEach(moldPart -> {
							long capa = getWeeklyCapaByMoldPart(moldPart);
							capacity[0] += capa;
						});
				value = capacity[0];
			} else {
				Map<Long, Long> capacities = new HashMap<>();
				BeanUtils.get(MoldPartRepository.class)//
						.findAll(applyProducibleMoldPartFilter(new BooleanBuilder(), brandId, productId, partId, supplierId, moldId, day))//
						.forEach(moldPart -> {
							long capacity = ValueUtils.toLong(capacities.get(moldPart.getPartId()), 0L);
							long capa = getWeeklyCapaByMoldPart(moldPart);
							capacity += capa;
							capacities.put(moldPart.getPartId(), capacity);
						});
				value = min(capacities);
			}
		}

		if (produced != null && value > 0 && !ObjectUtils.isEmpty(week)) {
			// Past
			if (compared < 0) {
				return produced;
			}
			// This Week
			else if (compared == 0) {
				Calendar cal = getCalendar(null);
				int days = 7 - cal.get(Calendar.DAY_OF_WEEK);
				if (days > 0) {
					value = value * days / 7 + produced;
				}
			}
		}
		return value;
	}

	private static long min(Map<Long, Long> capacities) {
		Long capacity = null;
		for (long item : capacities.values()) {
			if (capacity == null) {
				capacity = item;
				continue;
			}
			capacity = Math.min(item, capacity);
		}
		return ValueUtils.toLong(capacity, 0L);
	}

	private static long getWeeklyCapaByMoldPart(MoldPart moldPart) {
		Mold mold = moldPart.getMold();
		double wact = ValueUtils.toDouble(mold.getWeightedAverageCycleTime(), ValueUtils.toDouble(mold.getContractedCycleTime(), 0d));
		int cavity = ValueUtils.toInteger(moldPart.getCavity(), 0);
		// Production Hours per Day
		int prodHoursPerDay = ValueUtils.toInteger(mold.getShiftsPerDay(), 0);
		// Production Days per Week
		int prodDays = ValueUtils.toInteger(mold.getProductionDays(), 0);
		int targetUptimeRate = ValueUtils.toInteger(mold.getUptimeTarget(), 0);
		long value = calcWeeklyCapa(wact, cavity, prodHoursPerDay, prodDays, targetUptimeRate);
		return value;
	}

	public static long calcWeeklyCapa(Double avgCt, Integer cavityCount, Integer prodHoursPerDay, Integer prodDays, Integer targetUptimeRate) {
		if (avgCt == null || avgCt <= 0 || cavityCount == null || cavityCount <= 0 || prodHoursPerDay == null || prodHoursPerDay <= 0 || prodDays == null || prodDays <= 0
				|| targetUptimeRate == null || targetUptimeRate <= 0) {
			return 0L;
		}
		long value = ValueUtils.toLong((36000d / avgCt) * cavityCount * prodHoursPerDay * prodDays * (0.01d * targetUptimeRate), 0L);
		return value;
	}

	public static List<Part> getProducableParts(Long productId, String day) {
		List<Part> parts = BeanUtils.get(ProductRepository.class).findAllParts(//
				ProductPartGetIn.builder()//
						.productId(productId)//
						.producibleOnly(true)//
						.date(day)//
						.build(), //
				PageRequest.of(0, 1000)//
		);
		return parts;
	}

	private static BooleanBuilder toProducibleMoldPartFilter(Long partId, String day) {
		BooleanBuilder filter = applyProducibleMoldPartFilter(new BooleanBuilder(), null, null, partId, null, null, day);
		return filter;
	}

	private static BooleanBuilder applyProducibleMoldPartFilter(BooleanBuilder filter, Long brandId, Long productId, Long partId, List<Long> supplierId, List<Long> moldId,
			String day) {
		String dateStr = day.substring(0, 4) + "-" + day.substring(4, 6) + "-" + day.substring(6);
		Instant instant = DateUtils2.toInstant(dateStr, DatePattern.yyyy_MM_dd, Zone.GMT);

		if (brandId != null) {
			filter.and(Q.moldPart.part.categoryId.in(//
					JPAExpressions.select(Q.product.id).from(Q.product).where(Q.product.level.eq(3).and(Q.product.parentId.eq(brandId))))//
			);
		}
		if (productId != null) {
			filter.and(Q.moldPart.part.categoryId.eq(productId));
		}
		if (partId != null) {
			filter.and(Q.moldPart.partId.eq(partId));
		}
		if (ObjectUtils.isEmpty(supplierId)) {
			filter.and(Q.moldPart.mold.supplierCompanyId.isNotNull());
		} else {
			filter.and(Q.moldPart.mold.supplierCompanyId.in(supplierId));
		}
		if (!ObjectUtils.isEmpty(moldId)) {
			filter.and(Q.moldPart.mold.id.in(moldId));
		}
		QueryUtils.isMold(Q.moldPart.mold);
		filter.and(//
				(Q.moldPart.mold.counter.installedAt.isNotNull().and(Q.moldPart.mold.counter.installedAt.isNotEmpty()).and(Q.moldPart.mold.counter.installedAt.loe(dateStr)))//
						.or((Q.moldPart.mold.counter.installedAt.isNull().or(Q.moldPart.mold.counter.installedAt.isEmpty())).and(Q.moldPart.mold.counter.activatedAt.loe(instant)))//
		);
		filter.and(Q.moldPart.mold.maxCapacityPerWeek.gt(0));
		return filter;
	}

	public static String getThisWeek() {
		String timeValue = DateUtils2.format(DateUtils2.getInstant(), DatePattern.YYYYww, Zone.GMT);
		return timeValue;
	}

	public static String getNextDay(String day) {
		Instant tomorrow = DateUtils2.toInstant(day, DatePattern.yyyyMMdd, Zone.GMT).plus(Duration.ofDays(1L));
		String dateStr = DateUtils2.format(tomorrow, DatePattern.yyyyMMdd, Zone.GMT);
		return dateStr;
	}

	public static Calendar getCalendar(String day) {
		Calendar cal = Calendar.getInstance(TimeZone.getDefault());
		if (day != null) {
			int year = ValueUtils.toInteger(day.substring(0, 4), 0);
			int month = ValueUtils.toInteger(day.substring(4, 6), 0) - 1;
			int date = ValueUtils.toInteger(day.substring(6), 0);
			cal.set(year, month, date);
		}
		return cal;
	}

	public static void setPredicted(Object data, String week, Long produced) {
		if (data == null) {
			return;
		}

		Meta meta = getMeta(data.getClass());
		Long capacity = get(data, meta.getCapacity);
		long predicted = toPredicted(week, produced, capacity);
		set(data, meta.setPredicted, predicted);
	}

	public static ProductionSummary toSummary(String week, Long demand, Long produced, Long capacity) {
		ProductionSummary summary = new ProductionSummary();

		demand = demand == null ? 0L : demand;

		produced = produced == null ? 0L : produced;
		capacity = capacity == null ? 0L : capacity;
		long predicted = toPredicted(week, produced, capacity);

		double demandComplianceRate = ProductUtils.toDemandComplianceRate(demand, produced, predicted, capacity);
		PriorityType demandCompliance = ProductUtils.toDemandCompliance(demand, produced, predicted, capacity);

		summary.setDemand(demand);
		summary.setProduced(produced);
		summary.setPredicted(predicted);
		summary.setCapacity(capacity);
		summary.setDemandComplianceRate(demandComplianceRate);
		summary.setDemandCompliance(demandCompliance);

		return summary;
	}

	private static long toPredicted(String week, Long produced, Long capacity) {
		produced = produced == null ? 0L : produced;
		capacity = capacity == null ? 0L : capacity;
		long predicted = produced;

		if (capacity > 0 && !ObjectUtils.isEmpty(week)) {
			int compared = week.compareTo(ProductUtils.getThisWeek());
			// Future Week
			if (compared > 0) {
				predicted += (capacity * 0.8);
			}
			// This Week
			else if (compared == 0) {
				Calendar cal = getCalendar(null);
				int remaingDays = 7 - cal.get(Calendar.DAY_OF_WEEK);
				if (remaingDays > 0) {
					capacity = capacity * remaingDays / 7;
					predicted += (capacity * 0.8);
				}
			}
		}
		return predicted;
	}

	public static double toDemandComplianceRate(long demand, long produced, long predicted, long capacity) {
		if (demand == 0) {
			return predicted;
		}
		if (capacity - demand >= 0) {
			long remaining = predicted - demand;
			return remaining == 0L ? 0d : ValueUtils.toDouble(remaining, 0d) / ValueUtils.toDouble(demand, 0d);
		}
		long remaining = capacity - (demand * 2);
		return remaining == 0L ? 0d : ValueUtils.toDouble(remaining, 0d) / ValueUtils.toDouble(demand, 0d);
	}

	public static PriorityType toDemandCompliance(long demand, long produced, long predicted, long capacity) {
		PriorityType demandCompliance;
		if (predicted >= demand) {
			demandCompliance = PriorityType.HIGH;
		} else if (capacity >= demand) {
			demandCompliance = PriorityType.MEDIUM;
		} else {
			demandCompliance = PriorityType.LOW;
		}
		return demandCompliance;
	}

	public static void setDeliveryRiskLevel(Object data) {
		if (data == null) {
			return;
		}

		Meta meta = getMeta(data.getClass());
		long predicted = get(data, meta.getPredicted);
		long demand = get(data, meta.getDemand);
		long capacity = get(data, meta.getCapacity);
		if (demand > capacity) {
			set(data, meta.setDeliveryRiskLevel, PriorityType.HIGH);
			set(data, meta.setDeliverableRate, capacity == 0 ? -100000 : -(ValueUtils.toDouble(demand, 1d) / ValueUtils.toDouble(capacity, 1d)));
		} else if (demand > predicted) {
			set(data, meta.setDeliveryRiskLevel, PriorityType.MEDIUM);
			set(data, meta.setDeliverableRate, predicted == 0 ? 0d : (ValueUtils.toDouble(predicted, 1d) / ValueUtils.toDouble(demand, 1d)));
		} else {
			set(data, meta.setDeliveryRiskLevel, PriorityType.LOW);
			set(data, meta.setDeliverableRate, predicted == 0 ? 9999999d : (ValueUtils.toDouble(Math.max(1L, predicted), 1d) / ValueUtils.toDouble(Math.max(1L, demand), 1d)));
		}
	}

	public static ProdBarChart getProgressBarChart(Object data) {
//		if (data == null) {
//			return null;
//		}
//
//		Meta meta = getMeta(data.getClass());
//		long produced = get(data, meta.getProduced);
//		long predicted = get(data, meta.getPredicted);
//		long demand = get(data, meta.getDemand);
//		long capacity = get(data, meta.getCapacity);
//
//		String producedLabel = l("produced");
//		String predictedLabel = l("predicted");
//		String demandLabel = l("demand");
//		String capacityLabel = l("capacity");
//
//		ProdBarChart chart = new ProdBarChart(Arrays.asList(producedLabel, predictedLabel, demandLabel, capacityLabel),
//				Arrays.asList(new ProdBarChartRow("bar", producedLabel, "EA", "#7B3AAD", Arrays.asList(new ProdBarChartValue(null, new BigDecimal(produced), null))),
//						new ProdBarChartRow("bar", predictedLabel, "EA", "#C25FD3", Arrays.asList(new ProdBarChartValue(null, new BigDecimal(predicted), null))),
//						new ProdBarChartRow("bar", demandLabel, "EA", "#C0ADF2", Arrays.asList(new ProdBarChartValue(null, new BigDecimal(demand), null))),
//						new ProdBarChartRow("bar", capacityLabel, "EA", "#EB709A", Arrays.asList(new ProdBarChartValue(null, new BigDecimal(capacity), null)))),
//				null);
//		return chart;
		return null;
	}

//	private static String l(String key) {
//		return MessageUtils.get(key, null);
////		return ObjectUtils.isEmpty(key) ? key : ("$l{" + key + "}");
//	}

	private static class Meta {
		Method getProduced;
		Method getPredicted;
		Method setPredicted;
		Method getDemand;
		Method getCapacity;
		Method setDeliveryRiskLevel;
		Method setDeliverableRate;
	}

	private static long get(Object data, Method method) {
		try {
			return ValueUtils.toLong(method.invoke(data), 0L);
		} catch (IllegalAccessException | InvocationTargetException e) {
			throw new LogicException("INVOKE_FAIL", "Invokation Failure: " + data.getClass() + "." + method.getName());
		}
	}

	private static void set(Object data, Method method, Object value) {
		if (method == null) {
			return;
		}
		try {
			method.invoke(data, value);
		} catch (IllegalAccessException | InvocationTargetException e) {
			throw new LogicException("INVOKE_FAIL", "Invokation Failure: " + data.getClass() + "." + method.getName());
		}
	}

	private static final Map<Class<?>, Meta> META = new ConcurrentHashMap<>();

	private static Meta getMeta(Class<?> clazz) {
		LogicUtils.assertNotNull(clazz, "clazz");

		return CacheUtils.get(META, clazz, () -> {
			Meta meta = new Meta();
			meta.getProduced = getMethod(clazz, "getTotalProduced");
			meta.getPredicted = getMethod(clazz, "getPredictedQuantity");
			try {
				meta.setPredicted = getMethod(clazz, "setPredictedQuantity", Long.class);
			} catch (Exception e) {
				meta.setPredicted = getMethod(clazz, "setPredictedQuantity", long.class);
			}
			meta.getCapacity = getMethod(clazz, "getTotalMaxCapacity");
			try {
				meta.getDemand = getMethod(clazz, "getTotalProductionDemand");
				meta.setDeliveryRiskLevel = getMethod(clazz, "setDeliveryRiskLevel", PriorityType.class);
				meta.setDeliverableRate = getMethod(clazz, "setDeliverableRate", double.class);
			} catch (Exception e) {
				// skip
			}
			return meta;
		});
	}

	private static Method getMethod(Class<?> clazz, String name, Class<?>... paramTypes) {
		try {
			return clazz.getMethod(name, paramTypes);
		} catch (NoSuchMethodException e) {
			throw new LogicException("NO_SUCH_METHOD", "No Such Method: " + clazz.getName() + "." + name);
		}
	}

	public static JPQLQuery<Long> filterProductByBrand(Long brandId) {
		QCategory category = QCategory.category;
		return JPAExpressions.select(category.id).from(category).where(category.level.eq(3).and(category.parentId.eq(brandId)));
	}

	public static Map<Long, Map<String, ProductPartStat>> toPartStatMapByFieldAndTime(List<PartStat> list, String field, TimeScale groupBy) {
		Map<Long, Map<String, ProductPartStat>> map = new LinkedHashMap<>();
		if (ObjectUtils.isEmpty(list)) {
			return map;
		}
		list.forEach(partStat -> {
			Long key;
			if ("partId".equals(field)) {
				key = partStat.getPartId();
			} else if ("supplierId".equals(field)) {
				key = partStat.getSupplierId();
			} else if ("moldId".equals(field)) {
				key = partStat.getMoldId();
			} else {
				throw new LogicException("UNSUPPORTED_FIELD", "Unsupported Field: " + field);
			}
			Map<String, ProductPartStat> stats;
			if (map.containsKey(key)) {
				stats = map.get(key);
			} else {
				stats = new LinkedHashMap<>();
				map.put(key, stats);
			}
			String groupKey;
			if (TimeScale.YEAR.equals(groupBy)) {
				groupKey = partStat.getYear();
			} else if (TimeScale.QUARTER.equals(groupBy)) {
				groupKey = DateUtils2.toQuarterByMonth(partStat.getMonth());
			} else if (TimeScale.MONTH.equals(groupBy)) {
				groupKey = partStat.getMonth();
			} else if (TimeScale.WEEK.equals(groupBy)) {
				groupKey = partStat.getWeek();
			} else {
				groupKey = partStat.getDay();
			}

			if (!stats.containsKey(groupKey)) {
				stats.put(groupKey, new ProductPartStat(partStat.getProduced(), partStat.getProducedVal()));
			} else {
				ProductPartStat item = stats.get(groupKey);
				item.setProduced(item.getProduced() + partStat.getProduced());
				item.setProducedVal(item.getProducedVal() + partStat.getProducedVal());
			}
		});
		return map;
	}

}
