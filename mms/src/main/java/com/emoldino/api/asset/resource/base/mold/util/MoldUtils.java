package com.emoldino.api.asset.resource.base.mold.util;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

import com.emoldino.api.asset.resource.base.mold.dto.OptimalCycleTime;
import com.emoldino.api.asset.resource.base.mold.enumeration.ToolingStatus;
import com.emoldino.api.asset.resource.base.mold.service.MoldService;
import com.emoldino.api.common.resource.base.log.enumeration.ErrorType;
import com.emoldino.api.common.resource.base.log.util.LogUtils;
import com.emoldino.api.common.resource.base.option.dto.InactiveConfig;
import com.emoldino.api.common.resource.base.option.dto.RefurbPriorityConfig;
import com.emoldino.api.common.resource.base.option.dto.RefurbishmentConfig;
import com.emoldino.api.common.resource.base.option.dto.UtilizationConfig;
import com.emoldino.api.common.resource.base.option.enumeration.RefurbPriorityCheckBy;
import com.emoldino.api.common.resource.base.option.util.OptionUtils;
import com.emoldino.framework.enumeration.TimeScale;
import com.emoldino.framework.exception.LogicException;
import com.emoldino.framework.repository.Q;
import com.emoldino.framework.util.BeanUtils;
import com.emoldino.framework.util.DateUtils2;
import com.emoldino.framework.util.DateUtils2.DatePattern;
import com.emoldino.framework.util.DateUtils2.Zone;
import com.emoldino.framework.util.LogicUtils;
import com.emoldino.framework.util.Property;
import com.emoldino.framework.util.QueryUtils;
import com.emoldino.framework.util.ThreadUtils;
import com.emoldino.framework.util.TranUtils;
import com.emoldino.framework.util.ValueUtils;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.dsl.EntityPathBase;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.Data;
import saleson.api.mold.MoldStandardValueRepository;
import saleson.api.statistics.StatisticsRepository;
import saleson.common.enumeration.ConfigCategory;
import saleson.common.enumeration.ConfigOption;
import saleson.common.enumeration.EquipmentStatus;
import saleson.common.enumeration.OperatingStatus;
import saleson.model.Counter;
import saleson.model.Mold;
import saleson.model.MoldStandardValue;
import saleson.model.QStatistics;
import saleson.model.Statistics;

@Component
public class MoldUtils {

	private static MoldUtils instance;

	public MoldUtils() {
		instance = this;
	}

	@Value("${app.mold.optimal-cycle-time.min-cdata-count:30}")
	private int minCdataCount;
	@Value("${app.mold.optimal-cycle-time.min-cdata-count:10000}")
	private int maxCdataCount;

	public static List<Long> getMoldIds(Long partId, Long supplierId, String filterCode) {
		ValueUtils.assertNotEmpty(partId, "partId");
		ValueUtils.assertNotEmpty(supplierId, "supplierId");

		JPQLQuery<Long> query = BeanUtils.get(JPAQueryFactory.class)//
				.select(Q.mold.id)//
				.distinct()//
				.from(Q.mold)//
				.orderBy(Q.mold.id.asc());

		Set<EntityPathBase<?>> join = new HashSet<>();
		applyFilter(query, join, partId, supplierId, filterCode);

		return query.fetch();
	}

	public static long getMoldCount(Long partId, Long supplierId, String filterCode) {
		JPQLQuery<Long> query = BeanUtils.get(JPAQueryFactory.class)//
				.select(Q.mold.countDistinct())//
				.from(Q.mold);

		Set<EntityPathBase<?>> join = new HashSet<>();
		applyFilter(query, join, partId, supplierId, filterCode);

		return query.fetchOne();
	}

	private static void applyFilter(JPQLQuery<Long> query, Set<EntityPathBase<?>> join, Long partId, Long supplierId, String filterCode) {
		QueryUtils.applyMoldFilter(query, join, filterCode);
		BooleanBuilder filter = new BooleanBuilder();
		if (partId != null) {
			QueryUtils.joinPartByMold(query, join);
			filter.and(Q.part.id.eq(partId));
		}
		QueryUtils.joinSupplierByMold(query, join);
		if (supplierId != null) {
			filter.and(Q.supplier.id.eq(supplierId));
		}
		query.where(filter);
	}

	public static UtilizationConfig getUtilizationConfig() {
		UtilizationConfig config = OptionUtils.getContent("MOLD_UTILIZATION", UtilizationConfig.class, //
				UtilizationConfig.builder().low(30).medium(80).high(100).build());
		return config;
	}

	public static RefurbPriorityConfig getRefurbPriorityConfig() {
		RefurbishmentConfig data = OptionUtils.getFieldValues(ConfigCategory.REFURBISHMENT, RefurbishmentConfig.class);
		RefurbPriorityConfig config = new RefurbPriorityConfig();
		config.setEnabled(data.isEnabled());
		if (ConfigOption.UTILIZATION_RATE.equals(data.getConfigOption())) {
			config.setCheckBy(RefurbPriorityCheckBy.UTILIZATION_RATE);
			config.setLow(data.getLm());
			config.setMedium(data.getMh());
		} else {
			config.setCheckBy(RefurbPriorityCheckBy.REMAINING_MONTHS);
			config.setLow(data.getMlMonths());
			config.setMedium(data.getHmMonths());
		}
		return config;
	}

	public static InactiveConfig getInactiveConfig() {
		InactiveConfig config = OptionUtils.getContent("MOLD_INACTIVE", InactiveConfig.class, //
				InactiveConfig.builder().level1(12L).level2(24L).level3(36L).timeScale(TimeScale.MONTH).build());
		return config;
	}

	public static ToolingStatus toToolingStatus(Mold mold) {
		if (mold == null) {
			return null;
		}
		String moldStatus = toMoldStatus(mold);
		if ("WORKING".equals(moldStatus)) {
			return ToolingStatus.IN_PRODUCTION;
		} else if ("IDLE".equals(moldStatus)) {
			return ToolingStatus.IDLE;
		} else if ("NOT_WORKING".equals(moldStatus)) {
			return ToolingStatus.INACTIVE;
		} else if ("SENSOR_DETACHED".equals(moldStatus)) {
			return ToolingStatus.SENSOR_DETACHED;
		} else if ("SENSOR_OFFLINE".equals(moldStatus) || "DISCONNECTED".equals(moldStatus)) {
			return ToolingStatus.SENSOR_OFFLINE;
		} else if ("ON_STANDBY".equals(moldStatus)) {
			return ToolingStatus.ON_STANDBY;
		} else if ("NO_SENSOR".equals(moldStatus)) {
			return ToolingStatus.NO_SENSOR;
		} else if ("DISABLED".equals(moldStatus)) {
			return ToolingStatus.DISABLED;
		} else if ("DISPOSED".equals(moldStatus)) {
			return ToolingStatus.DISPOSED;
		}
		return ToolingStatus.UNKNOWN;
	}

	public static String toMoldStatus(Mold mold) {
		if (mold == null) {
			return null;
		}
		if (EquipmentStatus.DISPOSED.equals(mold.getEquipmentStatus()) || EquipmentStatus.DISCARDED.equals(mold.getEquipmentStatus())) {
			return "DISPOSED";
		}
		if (mold.isDeleted()) {
			return "DISABLED";
		}

		Counter counter = mold.getCounter();
		if (counter == null) {
			return "NO_SENSOR";
		}

		if (counter.getEquipmentStatus() == EquipmentStatus.DETACHED) {
			return "SENSOR_DETACHED";
		}

		OperatingStatus counterOpStatus = counter.getOperatingStatus();
		OperatingStatus opStatus = mold.getOperatingStatus();

		if (counterOpStatus == OperatingStatus.DISCONNECTED || opStatus == OperatingStatus.DISCONNECTED) {
			return "SENSOR_OFFLINE";
		}

		return opStatus != null ? //
				opStatus.toString() //
				: counterOpStatus != null ? counterOpStatus.toString() //
						: "ON_STANDBY";
	}

//	public static int getOptimalMaxCapacity(Long moldId, Integer contractedCycleTime, String dateStr) {
////		SUM(mld.SHIFT_PER_DAY * mpt.CAVITY * 3600 * COALESCE(mld.UPTIME_TARGET, 90) / (mld.StandardCycleTime / 10)) DATA
//		OptimalCycleTime oct = getOptimalCycleTime(moldId, contractedCycleTime, dateStr);
//
//		Mold mold = BeanUtils.get(MoldRepository.class).findById(moldId).orElse(null);
//		if (mold == null) {
//			return 0;
//		}
//
//		Double value = ValueUtils.toDouble(mold.getShiftsPerDay(), 24d) * ValueUtils.toInteger(mold.getTotalCavities(), 1) * 3600 * ValueUtils.toInteger(mold.getUptimeTarget(), 1)
//				/ oct.getValue() / 10;
//
//		return value.intValue();
//	}

//	@Deprecated
//	public static OptimalCycleTime getOptimalCycleTime(Mold mold, String dateStr, int minCdataCount, int maxCdataCount) {
//		Assert.notNull(mold, "mold is required");
//		OptimalCycleTime data = getOptimalCycleTime(mold.getId(), mold.getContractedCycleTime(), dateStr, minCdataCount, maxCdataCount);
//		return data;
//	}

	public static OptimalCycleTime getOptimalCycleTime(Long moldId, Integer contractedCycleTime, String dateStr) {
		LogicUtils.assertNotNull(moldId, "moldId");

		String key = "MoldUtils.getOptimalCycleTime." + ValueUtils.toString(moldId) + "." + ValueUtils.toString(contractedCycleTime) + "." + dateStr;
		return ThreadUtils.getProp(key, () -> {
			// Strategy of Optimal Cycle Time (OCT)
			String strategy = OptionUtils.getFieldValue(ConfigCategory.OPTIMAL_CYCLE_TIME, "strategy", "WACT");

			// Approved Cycle Time
			if ("ACT".equals(strategy)) {
				// Approved Cycle Time (ACT)
				double approvedCt = ValueUtils.toDouble(contractedCycleTime, 1d);
				OptimalCycleTime data = new OptimalCycleTime();
				data.setStrategy(strategy);
				data.setValue(approvedCt);
				return data;
			}
			// Weighted Average Cycle Time
			else {
				// Period Months of Optimal Cycle Time (OCT)
				int periodMonths = ValueUtils.toInteger(OptionUtils.getFieldValue(ConfigCategory.OPTIMAL_CYCLE_TIME, "periodMonths", null), 3);

				OptimalCycleTime data = _getWact(moldId, contractedCycleTime, dateStr, periodMonths);
				return data;
			}
		});
	}

	public static double getWact(Long moldId, Integer contractedCycleTime, String dateStr) {
		int periodMonths = getPeriodMonths();

		String key = "MoldUtils.getWact." + ValueUtils.toString(moldId) + "." + ValueUtils.toString(contractedCycleTime) + "." + dateStr;
		return ThreadUtils.getProp(key, () -> {
			OptimalCycleTime data = _getWact(moldId, contractedCycleTime, dateStr, periodMonths);
			return data.getValue();
		});
	}

	private static OptimalCycleTime _getWact(Long moldId, Integer contractedCycleTime, String dateStr, int periodMonths) {
		OptimalCycleTime data = new OptimalCycleTime();
		data.setStrategy("WACT");
		// Period Months of Optimal Cycle Time (OCT)
		data.setPeriodMonths(periodMonths);

		// Approved Cycle Time (ACT)
		double approvedCt = ValueUtils.toDouble(contractedCycleTime, 1d);

		MoldStandardValue msv = getStandardValue(moldId, contractedCycleTime, dateStr, periodMonths);
		if (msv == null) {
			data.setValue(approvedCt);
			return data;
		} else {
			populate(msv, data);
			return data;
		}
	}

	private static int getPeriodMonths() {
		String strategy = OptionUtils.getFieldValue(ConfigCategory.OPTIMAL_CYCLE_TIME, "strategy", "WACT");
		int periodMonths = "ACT".equals(strategy) ? 3 : ValueUtils.toInteger(OptionUtils.getFieldValue(ConfigCategory.OPTIMAL_CYCLE_TIME, "periodMonths", null), 3);
		return periodMonths;
	}

	public static void deleteStandardValue(Long moldId, Integer contractedCycleTime, String dateStr) {
		TranUtils.doTran(() -> {
			int periodMonths = getPeriodMonths();
			MoldStandardValue data = getStandardValue(moldId, contractedCycleTime, dateStr, periodMonths);
			if (data != null) {
				BeanUtils.get(MoldStandardValueRepository.class).delete(data);
			}
		});
	}

	private static MoldStandardValue getStandardValue(Long moldId, Integer contractedCycleTime, String dateStr, int periodMonths) {
		// Approved Cycle Time (ACT)
		double approvedCt = ValueUtils.toDouble(contractedCycleTime, 1d);

		// month as yyyyMM format from dateStr
		String month;
		// To yyyyMM
		{
			Instant instant = DateUtils2.newInstant();
			String thisMonth = DateUtils2.format(instant, DatePattern.yyyyMM, Zone.SYS);
			if (ObjectUtils.isEmpty(dateStr)) {
				month = thisMonth;
			} else {
				int len = dateStr.length();
				if (len <= 2) {
					month = thisMonth.substring(0, 4) + ValueUtils.pad(dateStr, 2, "left", "0");
				} else if (len == 6) {
					month = dateStr;
				} else if (len > 6) {
					month = dateStr.substring(0, 6);
				} else {
					month = dateStr;
				}

				if (month.compareTo(thisMonth) > 0) {
					return null;
				}
			}
		}

		Optional<MoldStandardValue> optional = BeanUtils.get(MoldService.class).getStandardValue(moldId, month, periodMonths, instance.minCdataCount, instance.maxCdataCount);
		if (optional.isPresent()) {
			return optional.get();
		}

		String fromTime;
		String toTime;
		{
			String zoneId = Zone.GMT;

			Instant time = DateUtils2.toInstant(month, DatePattern.yyyyMM, zoneId);
			Instant from = minusMonths(time, periodMonths + 1, zoneId);
			Instant to = minusMonths(time, 1, zoneId);

			fromTime = DateUtils2.format(from, DatePattern.yyyyMM, zoneId);
			toTime = DateUtils2.format(to, DatePattern.yyyyMM, zoneId);
		}

		AverageCycleTime averageCt = getAverageCycleTime(moldId, approvedCt, fromTime, toTime, instance.minCdataCount);

		MoldStandardValue msv = new MoldStandardValue(moldId, month, periodMonths, instance.minCdataCount, instance.maxCdataCount, averageCt.getCdataCount(), averageCt.getValue());
		try {
			BeanUtils.get(MoldService.class).saveStandardValue(msv);
		} catch (Exception e) {
			// SKIP
		}

		return msv;
	}

	public static AverageCycleTime getAverageCycleTime(long moldId, double approvedCt, String fromTime, String toTime, int minCdataCount) {
		int fromLen = fromTime.length();
		int toLen = toTime.length();

		double minShotAnHour = (3600d * 0.5d) / (approvedCt / 10d);

		Page<Statistics> page;
		try {
			QStatistics table = QStatistics.statistics;
			BooleanBuilder filter = new BooleanBuilder()//
					.and(table.moldId.eq(moldId))//
					.and(table.shotCount.gt(minShotAnHour))//
					.and(table.ct.lt(9999)).and(table.ct.gt(approvedCt / 3));
			if (fromLen == 6) {
				filter.and(table.month.goe(fromTime));
			} else if (fromLen == 8) {
				filter.and(table.day.goe(fromTime));
			} else {
				throw new LogicException("FROM_TIME_INVALID", new Property("fromTime", fromTime));
			}
			if (toLen == 6) {
				filter.and(table.month.loe(toTime));
			} else if (toLen == 8) {
				filter.and(table.day.loe(toTime));
			} else {
				throw new LogicException("TO_TIME_INVALID", new Property("toTime", toTime));
			}
			page = BeanUtils.get(StatisticsRepository.class).findAll(filter, PageRequest.of(0, instance.maxCdataCount, Direction.DESC, "lst"));
		} catch (Exception e) {
			LogUtils.saveErrorQuietly(ErrorType.LOGIC, "AVERAGE_CYCLE_TIME_BUG", HttpStatus.NOT_IMPLEMENTED, e.getMessage(), e);
			// TODO remove
			page = BeanUtils.get(StatisticsRepository.class).findAllByMoldIdAndShotCountGreaterThanAndCtGreaterThanAndMonthGreaterThanEqualAndMonthLessThanEqualOrderByLstDesc(
					moldId, (int) minShotAnHour, approvedCt / 3, fromTime, toTime, PageRequest.of(0, instance.maxCdataCount));
		}

		int cdataCount = page.getContent().size();
		double value;
		if (cdataCount < minCdataCount) {
			value = approvedCt;
		} else {
			// sum of (ShotCount * CycleTime) and sum of (ShotCount) of all ShotCounterData(cdata) with assuming 70% is correct
			double t = page.getContent().stream().limit(page.getContent().size() * 7 / 10).map(s -> s.getShotCount() * s.getCt()).mapToDouble(Double::doubleValue).sum();
			int m = page.getContent().stream().limit(page.getContent().size() * 7 / 10).map(s -> s.getShotCount()).mapToInt(Integer::intValue).sum();
			if (t == 0 || m == 0) {
				value = 0;
			} else {
				value = t / m;
			}
//			log.info("calc StandardCycleTime for mold {} in month {}", mold.getEquipmentCode(), month);
		}

		AverageCycleTime data = new AverageCycleTime();
		data.setCdataCount(cdataCount);
		data.setValue(value);
		return data;
	}

	@Data
	public static class AverageCycleTime {
		int cdataCount;
		double value;
	}

	private static void populate(MoldStandardValue msv, OptimalCycleTime data) {
		if (msv.getCdataCount() < msv.getMinCdataCount()) {
			data.setStrategy("ACT");
		}
		data.setValue(msv.getStandardValue());
	}

	@Deprecated
	/**
	 * TODO Test and replace with DateUtils2.plusMonths(instant, -months, zoneId)
	 * @param instant
	 * @param months
	 * @param zoneId
	 * @return
	 * @deprecated		use DateUtils2.plusMonths instead
	 */
	private static Instant minusMonths(Instant instant, int months, String zoneId) {
		LogicUtils.assertNotEmpty(zoneId, "zoneId");

//		return instant.minus(Duration.ofDays(28 * months));

		ZoneId zone = ZoneId.of(zoneId);
		LocalDateTime localDateTime = LocalDateTime.ofInstant(instant, zone);
		Instant value = localDateTime.minusMonths(months).atZone(zone).toInstant();
		return value;
	}

//	private static final String PROP_ZONE_ID = "MoldUtils.zoneId.";
//
//	public static String getZoneIdByMold(Mold mold) {
//		if (mold == null) {
//			return Zone.SYS;
//		}
//		String zoneId = ThreadUtils.getProp(PROP_ZONE_ID + mold.getId(), () -> LocationUtils.getZoneIdByLocationId(mold.getLocationId()));
//		return zoneId;
//	}
//
//	public static String getZoneIdByMoldId(Long moldId) {
//		if (moldId == null) {
//			return Zone.SYS;
//		}
//		String zoneId = ThreadUtils.getProp(PROP_ZONE_ID + moldId, () -> {
//			Mold mold = BeanUtils.get(MoldRepository.class).findById(moldId).orElse(null);
//			return getZoneIdByMold(mold);
//		});
//		return zoneId;
//	}
}
