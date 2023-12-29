package com.emoldino.api.analysis.resource.composite.proana.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
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

import org.apache.commons.math3.stat.descriptive.SummaryStatistics;
import org.apache.commons.math3.stat.descriptive.rank.Percentile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;

import com.emoldino.api.analysis.resource.base.data.repository.datacounter.DataCounter;
import com.emoldino.api.analysis.resource.base.data.repository.datacounter.DataCounter.CycleTime;
import com.emoldino.api.analysis.resource.base.data.repository.datacounter.DataCounterRepository;
import com.emoldino.api.analysis.resource.base.data.repository.datacounter.QDataCounter;
import com.emoldino.api.analysis.resource.base.data.util.MoldDataUtils;
import com.emoldino.api.analysis.resource.composite.datcol.dto.DatColProcessInfoOut;
import com.emoldino.api.analysis.resource.composite.datcol.service.accelerationdata.DatColAccelerationDataService;
import com.emoldino.api.analysis.resource.composite.proana.dto.ProAnaGetIn;
import com.emoldino.api.analysis.resource.composite.proana.dto.ProAnaGetOut;
import com.emoldino.api.analysis.resource.composite.proana.dto.ProAnaGetOut.ProAnaItem;
import com.emoldino.api.analysis.resource.composite.proana.dto.ProAnaPartsGetIn;
import com.emoldino.api.analysis.resource.composite.proana.dto.ProAnaPartsItem;
import com.emoldino.api.analysis.resource.composite.proana.service.part.ProAnaPartService;
import com.emoldino.api.common.resource.base.location.util.LocationUtils;
import com.emoldino.framework.dto.ListOut;
import com.emoldino.framework.enumeration.TimeScale;
import com.emoldino.framework.repository.Q;
import com.emoldino.framework.util.BeanUtils;
import com.emoldino.framework.util.Closure1ParamNoReturn;
import com.emoldino.framework.util.DateUtils2;
import com.emoldino.framework.util.DateUtils2.DatePattern;
import com.emoldino.framework.util.DateUtils2.Zone;
import com.emoldino.framework.util.ThreadUtils;
import com.emoldino.framework.util.ValueUtils;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.group.GroupBy;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.JPQLQueryFactory;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import saleson.api.mold.MoldRepository;
import saleson.model.Mold;
import saleson.model.QCounter;
import saleson.model.QStatistics;

@Service
@Transactional
public class ProAnaService {
	private static final List<TimeScale> TIME_SCALE_SUPPORTED = Arrays.asList(TimeScale.HOUR, TimeScale.DATE);
	private static final double ALL_STDEV_MULTIPLY = 3.0;
	private static final double FILTERED_STDEV_MULTIPLY = 2.0;

	public ProAnaGetOut get(ProAnaGetIn input, Pageable pageable) {
		ValueUtils.assertNotEmpty(input.getMoldId(), "moldId");
		ValueUtils.assertTimeSetting(input, TIME_SCALE_SUPPORTED);

		Mold mold = BeanUtils.get(MoldRepository.class).findById(input.getMoldId()).orElse(null);
		if (mold == null) {
			return new ProAnaGetOut(Collections.emptyList());
		}
		String counterCode = mold.getCounterCode();
		if (ObjectUtils.isEmpty(counterCode)) {
			return new ProAnaGetOut(Collections.emptyList());
		}

		ListOut<ProAnaPartsItem> parts;
		{
			ProAnaPartsGetIn reqin = ValueUtils.map(input, ProAnaPartsGetIn.class);
			parts = BeanUtils.get(ProAnaPartService.class).get(reqin);
		}

		Map<String, ProAnaItem> map = new TreeMap<>();

		BooleanBuilder filter = new BooleanBuilder();
		String zoneId = LocationUtils.getZoneIdByLocation(mold.getLocation());
		String datePattern;
		int substringIndex = 0;
		String fromGmt;
		String beforeGmt;
		{
			Instant fromInst = null;
			Instant beforeInst = null;
			QDataCounter table = QDataCounter.dataCounter;
			filter.and(table.counterId.eq(counterCode));
			if (TimeScale.HOUR.equals(input.getTimeScale())) {
				datePattern = DatePattern.yyyyMMddHHmm;
				substringIndex = 10;

				fromInst = DateUtils2.toInstant(input.getTimeValue(), DatePattern.yyyyMMddHH, zoneId);
				beforeInst = fromInst.plus(Duration.ofHours(1));
				filter.and(table.shotEndTime.goe(DateUtils2.format(fromInst, DatePattern.yyyyMMddHHmmss, Zone.GMT)));
				filter.and(table.shotStartTime.lt(DateUtils2.format(beforeInst, DatePattern.yyyyMMddHHmmss, Zone.GMT)));

				fromGmt = DateUtils2.format(fromInst, datePattern, Zone.GMT);
				beforeGmt = DateUtils2.format(beforeInst, datePattern, Zone.GMT);

				String next = DateUtils2.format(fromInst, datePattern, zoneId);
				String before = DateUtils2.format(beforeInst, datePattern, zoneId);

				Instant nextInst;
				do {
					nextInst = DateUtils2.toInstant(next, datePattern, zoneId);
					ProAnaItem item = ProAnaItem.builder().time(next.substring(substringIndex)).build();
					Ranges ranges = getRanges(mold, nextInst, zoneId);
					item.setRefInjectionTime(new BigDecimal(ranges.meanIt).setScale(2, RoundingMode.HALF_UP));
					item.setInjectionTimeLl(new BigDecimal(Math.min(ranges.itRange.getFirst(), ranges.itRange.getSecond())).setScale(2, RoundingMode.HALF_UP));
					item.setInjectionTimeUl(new BigDecimal(Math.max(ranges.itRange.getFirst(), ranges.itRange.getSecond())).setScale(2, RoundingMode.HALF_UP));
					item.setInjectionTimeList(new ArrayList<Double>());
					item.setRefPackingTime(new BigDecimal(ranges.meanPt).setScale(2, RoundingMode.HALF_UP));
					item.setPackingTimeLl(new BigDecimal(Math.min(ranges.ptRange.getFirst(), ranges.ptRange.getSecond())).setScale(2, RoundingMode.HALF_UP));
					item.setPackingTimeUl(new BigDecimal(Math.max(ranges.ptRange.getFirst(), ranges.ptRange.getSecond())).setScale(2, RoundingMode.HALF_UP));
					item.setPackingTimeList(new ArrayList<Double>());
					item.setRefCoolingTime(new BigDecimal(ranges.meanCt).setScale(2, RoundingMode.HALF_UP));
					item.setCoolingTimeLl(new BigDecimal(Math.min(ranges.ctRange.getFirst(), ranges.ctRange.getSecond())).setScale(2, RoundingMode.HALF_UP));
					item.setCoolingTimeUl(new BigDecimal(Math.max(ranges.ctRange.getFirst(), ranges.ctRange.getSecond())).setScale(2, RoundingMode.HALF_UP));
					item.setCoolingTimeList(new ArrayList<Double>());
					item.setRefInjectionPressure(new BigDecimal(ranges.meanIp).setScale(2, RoundingMode.HALF_UP));
					item.setInjectionPressureLl(new BigDecimal(Math.min(ranges.ipRange.getFirst(), ranges.ipRange.getSecond())).setScale(2, RoundingMode.HALF_UP));
					item.setInjectionPressureUl(new BigDecimal(Math.max(ranges.ipRange.getFirst(), ranges.ipRange.getSecond())).setScale(2, RoundingMode.HALF_UP));
					item.setInjectionPressureList(new ArrayList<Double>());
					item.setRefPackingPressure(new BigDecimal(ranges.meanPp).setScale(2, RoundingMode.HALF_UP));
					item.setPackingPressureLl(new BigDecimal(Math.min(ranges.ppRange.getFirst(), ranges.ppRange.getSecond())).setScale(2, RoundingMode.HALF_UP));
					item.setPackingPressureUl(new BigDecimal(Math.max(ranges.ppRange.getFirst(), ranges.ppRange.getSecond())).setScale(2, RoundingMode.HALF_UP));
					item.setPackingPressureList(new ArrayList<Double>());
					map.put(next, item);
				} while ((//
				next = DateUtils2.format(//
						nextInst.plus(Duration.ofMinutes(1)), //
						datePattern, zoneId)//
				).compareTo(before) < 0);

			} else if (TimeScale.DATE.equals(input.getTimeScale())) {
				datePattern = DatePattern.yyyyMMddHH;
				substringIndex = 8;

				fromInst = DateUtils2.toInstant(input.getTimeValue(), DatePattern.yyyyMMdd, zoneId);
				beforeInst = fromInst.plus(Duration.ofDays(1));
				filter.and(table.shotEndTime.goe(DateUtils2.format(fromInst, DatePattern.yyyyMMddHHmmss, Zone.GMT)));
				filter.and(table.shotStartTime.lt(DateUtils2.format(beforeInst, DatePattern.yyyyMMddHHmmss, Zone.GMT)));

				fromGmt = DateUtils2.format(fromInst, datePattern, Zone.GMT);
				beforeGmt = DateUtils2.format(beforeInst, datePattern, Zone.GMT);

				String next = DateUtils2.format(fromInst, datePattern, zoneId);
				String before = DateUtils2.format(beforeInst, datePattern, zoneId);
				
				// (Temporary) Set Shot Count , TO-DO : Calculate Shot Count Logic
				Map<String, Integer> shotsTimeMapAll = new LinkedHashMap<>();				
				shotsTimeMapAll = BeanUtils.get(ProAnaService.class).getShotCount(mold, input.getTimeValue()); 

				Instant nextInst;
				do {
					nextInst = DateUtils2.toInstant(next, datePattern, zoneId);
					ProAnaItem item = ProAnaItem.builder().time(next.substring(substringIndex)).build();
					item.setInjectionTimeList(new ArrayList<Double>());
					item.setPackingTimeList(new ArrayList<Double>());
					item.setCoolingTimeList(new ArrayList<Double>());
					item.setInjectionPressureList(new ArrayList<Double>());
					item.setPackingPressureList(new ArrayList<Double>());		
					if(!ObjectUtils.isEmpty(shotsTimeMapAll.get(input.getTimeValue() + item.getTime()))) {
						item.setShotCount(shotsTimeMapAll.get(input.getTimeValue() + item.getTime()));		
					}								
					map.put(next, item);
				} while ((//
				next = DateUtils2.format(//
						nextInst.plus(Duration.ofHours(1)), //
						datePattern, zoneId)//
				).compareTo(before) < 0);
			} else {
				return new ProAnaGetOut(Collections.emptyList());
			}
		}
				
		procAccData(filter, new Closure1ParamNoReturn<DataCounter>() {
			public void execute(DataCounter dataCounter) {
				Long dataId = dataCounter.getDataId();

				ArrayList<DatColProcessInfoOut> accDataList;
				try {
					accDataList = BeanUtils.get(DatColAccelerationDataService.class).getAccData2(dataId);
				} catch (Exception e) {
					return;
				}
				if (ObjectUtils.isEmpty(accDataList)) {
					return;
				}

				// TO-DO : Calculate ShotCount (Statistics와 동일하게 변경) 
//				Map<String, List<CycleTime>> shotsTimeMap = new LinkedHashMap<>();
//				MoldDataUtils.populateByTime(dataCounter, shotsTimeMap, null);

				for (DatColProcessInfoOut accData : accDataList) {
					String timeGmt = accData.getMeasuredTime();
					if (timeGmt.compareTo(fromGmt) < 0 || timeGmt.compareTo(beforeGmt) >= 0) {
						continue;
					}

					Instant instant = DateUtils2.toInstant(timeGmt, DatePattern.yyyyMMddHHmmss, Zone.GMT);
					String time = DateUtils2.format(instant, datePattern, zoneId);
					if (!map.containsKey(time)) {
						continue;
					}

					double it = ValueUtils.toDouble(accData.getInjectionTime(), 0d);
					double pt = ValueUtils.toDouble(accData.getPackingTime(), 0d);
					double ct = ValueUtils.toDouble(accData.getCoolingTime(), 0d);
					double ip = ValueUtils.toDouble(accData.getInjectionPressureIndex(), 0d);
					double pp = ValueUtils.toDouble(accData.getPackingPressureIndex(), 0d);
					
					ProAnaItem item = map.get(time);
					
//					shotsTimeMap.forEach((shotEndTime, shots) -> {
//						int shotCount = shots.size();
//						item.setShotCount(item.getShotCount() + shotCount);
//					});
					

					if (it != 0.0)
						item.getInjectionTimeList().add(it);
					if (pt != 0.0)
						item.getPackingTimeList().add(pt);
					if (ct != 0.0)
						item.getCoolingTimeList().add(ct);
					if (ip != 0.0)
						item.getInjectionPressureList().add(ip);
					if (pp != 0.0)
						item.getPackingPressureList().add(pp);

					Ranges ranges = getRanges(mold, instant, zoneId);
					if (ranges != null) {
						boolean itAbnormal = isAbnormal(it, ranges.itRange);
						boolean ptAbnormal = isAbnormal(pt, ranges.ptRange);
						boolean ctAbnormal = isAbnormal(ct, ranges.ctRange);
						boolean ipAbnormal = isAbnormal(ip, ranges.ipRange);
						boolean ppAbnormal = isAbnormal(pp, ranges.ppRange);

						boolean abnormal = itAbnormal || ptAbnormal || ctAbnormal || ipAbnormal || ppAbnormal;

						if (abnormal) {
							// 2023.04.21 Mickey.Park
							// DATE 그래프는 Abnormal 상태(빨간색) 보이지 않기 위해 주석처리 한다.
							// item.setAbnormal(abnormal);
							item.setAbnormalCount(item.getAbnormalCount() + 5);
						}

						if (itAbnormal)
							item.setItAbnormal(itAbnormal);
						if (ptAbnormal)
							item.setPtAbnormal(ptAbnormal);
						if (ctAbnormal)
							item.setCtAbnormal(ctAbnormal);
						if (ipAbnormal)
							item.setIpAbnormal(ipAbnormal);
						if (ppAbnormal)
							item.setPpAbnormal(ppAbnormal);
					}

				}
			}
		});

		if (TimeScale.HOUR.equals(input.getTimeScale())) {
			map.forEach((key, value) -> {

				if (!ObjectUtils.isEmpty(value.getInjectionTimeList())) {
					Double itResult = value.getInjectionTimeList().stream()//
							.mapToDouble(Double::doubleValue) //
							.average() //
							.orElse(Double.NaN);
					value.setInjectionTime(new BigDecimal(itResult).setScale(2, RoundingMode.HALF_UP));
				}

				if (!ObjectUtils.isEmpty(value.getPackingTimeList())) {
					Double ptResult = value.getPackingTimeList().stream()//
							.mapToDouble(Double::doubleValue) //
							.average() //
							.orElse(Double.NaN);
					value.setPackingTime(new BigDecimal(ptResult).setScale(2, RoundingMode.HALF_UP));
				}

				if (!ObjectUtils.isEmpty(value.getCoolingTimeList())) {
					Double ctResult = value.getCoolingTimeList().stream()//
							.mapToDouble(Double::doubleValue) //
							.average() //
							.orElse(Double.NaN);
					value.setCoolingTime(new BigDecimal(ctResult).setScale(2, RoundingMode.HALF_UP));
				}

				if (!ObjectUtils.isEmpty(value.getInjectionPressureList())) {
					Double ipResult = value.getInjectionPressureList().stream()//
							.mapToDouble(Double::doubleValue) //
							.average() //
							.orElse(Double.NaN);
					value.setInjectionPressure(new BigDecimal(ipResult).setScale(2, RoundingMode.HALF_UP));
				}

				if (!ObjectUtils.isEmpty(value.getPackingPressureList())) {
					Double ppResult = value.getPackingPressureList().stream()//
							.mapToDouble(Double::doubleValue) //
							.average() //
							.orElse(Double.NaN);
					value.setPackingPressure(new BigDecimal(ppResult).setScale(2, RoundingMode.HALF_UP));
				}
			});
		} else if (TimeScale.DATE.equals(input.getTimeScale())) {
			// Calculation of acceleration data by time 
			map.forEach((key, value) -> {
				AccResult itResult = calAccHourValues(value.getInjectionTimeList());
				value.setInjectionTime(itResult == null ? null : new BigDecimal(itResult.getResultValue()).setScale(2, RoundingMode.HALF_UP));
				value.setInjectionMaxTime(itResult == null ? null : new BigDecimal(itResult.getMaxValue()).setScale(2, RoundingMode.HALF_UP));
				value.setInjectionMinTime(itResult == null ? null : new BigDecimal(itResult.getMinValue()).setScale(2, RoundingMode.HALF_UP));

				AccResult ptResult = calAccHourValues(value.getPackingTimeList());
				value.setPackingTime(ptResult == null ? null : new BigDecimal(ptResult.getResultValue()).setScale(2, RoundingMode.HALF_UP));
				value.setPackingMaxTime(ptResult == null ? null : new BigDecimal(ptResult.getMaxValue()).setScale(2, RoundingMode.HALF_UP));
				value.setPackingMinTime(ptResult == null ? null : new BigDecimal(ptResult.getMinValue()).setScale(2, RoundingMode.HALF_UP));

				AccResult ctResult = calAccHourValues(value.getCoolingTimeList());
				value.setCoolingTime(ctResult == null ? null : new BigDecimal(ctResult.getResultValue()).setScale(2, RoundingMode.HALF_UP));
				value.setCoolingMaxTime(ctResult == null ? null : new BigDecimal(ctResult.getMaxValue()).setScale(2, RoundingMode.HALF_UP));
				value.setCoolingMinTime(ctResult == null ? null : new BigDecimal(ctResult.getMinValue()).setScale(2, RoundingMode.HALF_UP));

				AccResult ipResult = calAccHourValues(value.getInjectionPressureList());
				value.setInjectionPressure(ipResult == null ? null : new BigDecimal(ipResult.getResultValue()).setScale(2, RoundingMode.HALF_UP));
				value.setInjectionMaxPressure(ipResult == null ? null : new BigDecimal(ipResult.getMaxValue()).setScale(2, RoundingMode.HALF_UP));
				value.setInjectionMinPressure(ipResult == null ? null : new BigDecimal(ipResult.getMinValue()).setScale(2, RoundingMode.HALF_UP));

				AccResult ppResult = calAccHourValues(value.getPackingPressureList());
				value.setPackingPressure(ppResult == null ? null : new BigDecimal(ppResult.getResultValue()).setScale(2, RoundingMode.HALF_UP));
				value.setPackingMaxPressure(ppResult == null ? null : new BigDecimal(ppResult.getMaxValue()).setScale(2, RoundingMode.HALF_UP));
				value.setPackingMinPressure(ppResult == null ? null : new BigDecimal(ppResult.getMinValue()).setScale(2, RoundingMode.HALF_UP));
			});

		}
		ProAnaGetOut output = new ProAnaGetOut(new ArrayList<>(map.values()));
		output.setMoldId(mold.getId());
		output.setMoldCode(mold.getEquipmentCode());
		output.setParts(parts.getContent());
		return output;
	}

	private Map<String, Integer> getShotCount(Mold mold, String day) {
		return BeanUtils.get(JPAQueryFactory.class) //
				.select(Projections.constructor(MoldScData.class, //
						Q.statistics.hour, //
						Q.statistics.shotCount.sum().as("shotCount")))//
				.from(Q.statistics) //
				.leftJoin(Q.counter).on(Q.statistics.ci.eq(Q.counter.equipmentCode)) //
				.where(Q.statistics.day.eq(day), //
						Q.statistics.moldId.eq(mold.getId()), //
						Q.statistics.shotCount.isNotNull(), //
						Q.statistics.ct.gt(0), //
						Q.statistics.ct.ne(9999.0), //
						Q.statistics.ct.gt(0.0).or(Q.statistics.firstData.eq(true)), //
						Q.statistics.shotCount.gt(0).or(Q.statistics.firstData.eq(true))) //
				.groupBy(Q.statistics.moldId, Q.statistics.hour) //
				.orderBy(Q.statistics.moldId.asc(), Q.statistics.hour.asc()) //
				.transform(GroupBy.groupBy(Q.statistics.hour).as(Q.statistics.shotCount.sum()));
	}

	private void procAccData(BooleanBuilder filter, Closure1ParamNoReturn<DataCounter> closure) {
		int counter = 0;
		int pageNo = 0;
		Page<DataCounter> page;
		while (counter++ < 10000//
				&& !(page = BeanUtils.get(DataCounterRepository.class).findAll(filter, PageRequest.of(pageNo++, 100, Direction.ASC, "shotStartTime"))).isEmpty()) {
			for (DataCounter data : page.getContent()) {
				if (ObjectUtils.isEmpty(data.getShotEndTime())) {
					continue;
				}

				boolean process = !ObjectUtils.isEmpty(data.getCycleTimes());
				if (!process) {
					continue;
				}
				closure.execute(data);
			}
		}
	}

	private static boolean isAbnormal(double value, Pair<Double, Double> range) {
		if (value == 0d || range == null || range.getFirst() == null || range.getSecond() == null || (range.getFirst().equals(0d) && range.getSecond().equals(0d))) {
			return false;
		}
		double min = Math.min(range.getFirst(), range.getSecond());
		double max = Math.max(range.getFirst(), range.getSecond());
		return (min > value || value > max);
	}

	private Ranges getRanges(Mold mold, Instant instant, String zoneId) {
		if (instant == null) {
			return null;
		}
		String week = DateUtils2.format(instant.minus(Duration.ofDays(7)), DatePattern.YYYYww, zoneId);
		Ranges ranges = getRanges(mold, week, zoneId, 0);
		return ranges;
	}

	private Ranges getRanges(Mold mold, String week, String zoneId, int depth) {
		Ranges values = ThreadUtils.getProp("ProAna." + mold.getId() + "." + week, () -> {
			Integer totalShotCount;
			{
				QStatistics table = QStatistics.statistics;
				JPAQuery<Integer> query = BeanUtils.get(JPAQueryFactory.class)//
						.select(table.shotCount.sum().coalesce(0))//
						.from(table)//
						.where(new BooleanBuilder()//
								.and(table.moldId.eq(mold.getId()))//
								.and(table.week.eq(week)));
				totalShotCount = query.fetchOne();
			}

			int dailyCapacity = ValueUtils.toInteger(mold.getDailyMaxCapacity(), 0);
			if (dailyCapacity <= 0) {
				dailyCapacity = ValueUtils.toInteger(mold.getMaxCapacityPerWeek(), 7) / 7;
			}
			dailyCapacity /= 10;

			if (totalShotCount < dailyCapacity) {
				if (depth >= 3) {
					return emptyRanges();
				}

				String prevWeek = DateUtils2.format(//
						DateUtils2.toInstant(week, DatePattern.YYYYww, zoneId).minus(Duration.ofDays(7)), //
						DatePattern.YYYYww, zoneId);
				Ranges ranges = getRanges(mold, prevWeek, zoneId, depth + 1);
				return ranges;
			}

			String counterCode = mold.getCounterCode();
			if (ObjectUtils.isEmpty(counterCode)) {
				return emptyRanges();
			}

			BooleanBuilder filter = new BooleanBuilder();
			QDataCounter table = QDataCounter.dataCounter;
			filter.and(table.counterId.eq(counterCode));

			Instant fromInst = DateUtils2.toInstant(week, DatePattern.YYYYww, zoneId);
			Instant beforeInst = fromInst.plus(Duration.ofDays(7));
			filter.and(table.shotEndTime.goe(DateUtils2.format(fromInst, DatePattern.yyyyMMddHHmmss, Zone.GMT)));
			filter.and(table.shotStartTime.lt(DateUtils2.format(beforeInst, DatePattern.yyyyMMddHHmmss, Zone.GMT)));

			List<Double> itList = new ArrayList<>(); // List of Injection Time 
			List<Double> ptList = new ArrayList<>(); // List of Packing Time
			List<Double> ctList = new ArrayList<>(); // List of Cooling Time
			List<Double> ipList = new ArrayList<>(); // List of Injection Pressure
			List<Double> ppList = new ArrayList<>(); // List of Packing Pressure

			procAccData(filter, new Closure1ParamNoReturn<DataCounter>() {
				public void execute(DataCounter dataCounter) {
					Long dataId = dataCounter.getDataId();

					ArrayList<DatColProcessInfoOut> accDataList;
					try {
						accDataList = BeanUtils.get(DatColAccelerationDataService.class).getAccData2(dataId);
					} catch (Exception e) {
						return;
					}
					if (ObjectUtils.isEmpty(accDataList)) {
						return;
					}

					double shotCount = ValueUtils.toDouble(dataCounter.getCycleTimes().size(), 0d);
					if (shotCount == 0) {
						return;
					}

					for (DatColProcessInfoOut accData : accDataList) {
						if (accData.getInjectionTime() != 0d) {
							itList.add(ValueUtils.toDouble(accData.getInjectionTime(), 0d));
						}

						if (accData.getPackingTime() != 0d) {
							ptList.add(ValueUtils.toDouble(accData.getPackingTime(), 0d));
						}

						if (accData.getCoolingTime() != 0d) {
							ctList.add(ValueUtils.toDouble(accData.getCoolingTime(), 0d));
						}

						if (accData.getInjectionPressureIndex() != 0d) {
							ipList.add(ValueUtils.toDouble(accData.getInjectionPressureIndex(), 0d));
						}

						if (accData.getPackingPressureIndex() != 0d) {
							ppList.add(ValueUtils.toDouble(accData.getPackingPressureIndex(), 0d));
						}
					}
				}
			});

			// 1. Calculate 1st Range (Extracted Data)
			Pair<Double, Pair<Double, Double>> itAllRange = getRange(itList, ALL_STDEV_MULTIPLY, true, false);
			Pair<Double, Pair<Double, Double>> ptAllRange = getRange(ptList, ALL_STDEV_MULTIPLY, true, false);
			Pair<Double, Pair<Double, Double>> ctAllRange = getRange(ctList, ALL_STDEV_MULTIPLY, true, false);
			Pair<Double, Pair<Double, Double>> ipAllRange = getRange(ipList, ALL_STDEV_MULTIPLY, false, false);
			Pair<Double, Pair<Double, Double>> ppAllRange = getRange(ppList, ALL_STDEV_MULTIPLY, false, false);

			// 2. Calculate 2nd Range (Filtered Data)
			List<Double> filteredItList = itList.stream() //
					.filter(d -> d > itAllRange.getSecond().getFirst() && d < itAllRange.getSecond().getSecond()) //
					.collect(Collectors.toList());

			List<Double> filteredPtList = ptList.stream() //
					.filter(d -> d > ptAllRange.getSecond().getFirst() && d < ptAllRange.getSecond().getSecond()) //
					.collect(Collectors.toList());

			List<Double> filteredCtList = ctList.stream() //
					.filter(d -> d > ctAllRange.getSecond().getFirst() && d < ctAllRange.getSecond().getSecond()) //
					.collect(Collectors.toList());

			List<Double> filteredIpList = ipList.stream() //
					.filter(d -> d > ipAllRange.getSecond().getFirst() && d < ipAllRange.getSecond().getSecond()) //
					.collect(Collectors.toList());

			List<Double> filteredPpList = ppList.stream() //
					.filter(d -> d > ppAllRange.getSecond().getFirst() && d < ppAllRange.getSecond().getSecond()) //
					.collect(Collectors.toList());

			Ranges ranges = new Ranges();
			ranges.itRange = getRange(filteredItList, FILTERED_STDEV_MULTIPLY, true, true).getSecond();
			ranges.meanIt = itAllRange.getFirst();
			ranges.ptRange = getRange(filteredPtList, FILTERED_STDEV_MULTIPLY, true, true).getSecond();
			ranges.meanPt = ptAllRange.getFirst();
			ranges.ctRange = getRange(filteredCtList, FILTERED_STDEV_MULTIPLY, true, true).getSecond();
			ranges.meanCt = ctAllRange.getFirst();
			ranges.ipRange = getRange(filteredIpList, FILTERED_STDEV_MULTIPLY, false, true).getSecond();
			ranges.meanIp = ipAllRange.getFirst();
			ranges.ppRange = getRange(filteredPpList, FILTERED_STDEV_MULTIPLY, false, true).getSecond();
			ranges.meanPp = ppAllRange.getFirst();
			return ranges;
		});
		return values;
	}

	private Pair<Double, Pair<Double, Double>> getRange(List<Double> list, double multiply, boolean isTime, boolean isFinal) {

		if(ObjectUtils.isEmpty(list)) {
			return Pair.of(0d, Pair.of(0d, 0d));
		}
		
		SummaryStatistics stat = new SummaryStatistics();
		for (double value : list) {
			stat.addValue(value);
		}
		
		// 2023.05.09 Mickey Park
		// Request for Dr.Mason - 시간의 경우 +-0.5, 압력의 경우 +-5 처리
		double lowerLimit = 0.0;
		double upperLimit = 0.0;
		double tempTimeCalVal = 0.5;
		double tempPressureCalVal = 5.0;
		if (isFinal) {
			if (isTime) {
				lowerLimit = stat.getMean() - (multiply * stat.getStandardDeviation()) - tempTimeCalVal;
				upperLimit = stat.getMean() + (multiply * stat.getStandardDeviation()) + tempTimeCalVal;
			} else {
				lowerLimit = stat.getMean() - (multiply * stat.getStandardDeviation()) - tempPressureCalVal;
				upperLimit = stat.getMean() + (multiply * stat.getStandardDeviation()) + tempPressureCalVal;
			}
		} else {
			lowerLimit = stat.getMean() - (multiply * stat.getStandardDeviation());
			upperLimit = stat.getMean() + (multiply * stat.getStandardDeviation());
		}

		return Pair.of(stat.getMean(), Pair.of(Math.max(0d, lowerLimit), upperLimit));
	}

	private static Ranges emptyRanges() {
		Ranges ranges = new Ranges();
		ranges.meanIt = 0d;
		ranges.itRange = Pair.of(0d, 0d);
		ranges.meanPt = 0d;
		ranges.ptRange = Pair.of(0d, 0d);
		ranges.meanCt = 0d;
		ranges.ctRange = Pair.of(0d, 0d);
		ranges.meanIp = 0d;
		ranges.ipRange = Pair.of(0d, 0d);
		ranges.meanPp = 0d;
		ranges.ppRange = Pair.of(0d, 0d);
		return ranges;
	}

	private static class Ranges {
		Double meanIt;
		Pair<Double, Double> itRange;
		Double meanPt;
		Pair<Double, Double> ptRange;
		Double meanCt;
		Pair<Double, Double> ctRange;
		Double meanIp;
		Pair<Double, Double> ipRange;
		Double meanPp;
		Pair<Double, Double> ppRange;
	}

	private AccResult calAccHourValues(List<Double> list) {

		if (ObjectUtils.isEmpty(list)) {
			return null;
		}

		// 1. convert double array
		double[] array = list.stream().mapToDouble(Double::doubleValue).toArray();

		// 2. Calculate Quart 1(25%), Quart 3(75%)
		Percentile percentile = new Percentile();
		double q1 = percentile.evaluate(array, 25);
		double q3 = percentile.evaluate(array, 75);

		// 3. Output
		Double resultValue = list.stream() //
				.filter(d -> d >= q1 && d <= q3) //
				.mapToDouble(Double::doubleValue) //
				.average() //
				.orElse(Double.NaN);

		Double maxValue = list.stream() //
				.filter(d -> d >= q1 && d <= q3) //
				.mapToDouble(Double::doubleValue) //
				.max() //
				.orElse(Double.NaN);

		Double minValue = list.stream() //
				.filter(d -> d >= q1 && d <= q3) //
				.mapToDouble(Double::doubleValue) //
				.min() //
				.orElse(Double.NaN);

		AccResult out = AccResult.builder() //
				.resultValue(resultValue) //
				.maxValue(maxValue) //
				.minValue(minValue) //
				.build();
		return out;
	}

	@Builder
	@Data
	private static class AccResult {
		Double minValue;
		Double maxValue;
		Double resultValue;
	}
		
	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	private static class MoldScData{
		String hour;
		Integer shotCount;
	}

}
