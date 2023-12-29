package com.emoldino.api.analysis.resource.composite.proana.service.Range;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import com.emoldino.api.analysis.resource.base.data.repository.datacounter.DataCounter;
import com.emoldino.api.analysis.resource.base.data.repository.datacounter.DataCounterRepository;
import com.emoldino.api.analysis.resource.base.data.repository.datacounter.QDataCounter;
import com.emoldino.api.analysis.resource.composite.datcol.dto.DatColProcessInfoOut;
import com.emoldino.api.analysis.resource.composite.datcol.service.accelerationdata.DatColAccelerationDataService;
import com.emoldino.api.analysis.resource.composite.proana.dto.ProAnaGetIn;
import com.emoldino.api.analysis.resource.composite.proana.dto.ProAnaGetOut;
import com.emoldino.api.common.resource.base.location.util.LocationUtils;
import com.emoldino.framework.exception.BizException;
import com.emoldino.framework.util.BeanUtils;
import com.emoldino.framework.util.Closure1ParamNoReturn;
import com.emoldino.framework.util.DateUtils2;
import com.emoldino.framework.util.Property;
import com.emoldino.framework.util.ThreadUtils;
import com.emoldino.framework.util.ValueUtils;
import com.emoldino.framework.util.DateUtils2.DatePattern;
import com.emoldino.framework.util.DateUtils2.Zone;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;

import saleson.api.mold.MoldRepository;
import saleson.model.Mold;
import saleson.model.QStatistics;

@Service
public class ProAnaRangeService {
	
	// For getRange Test
	public Ranges getRangesTest(ProAnaGetIn input) {		
		Mold mold = BeanUtils.get(MoldRepository.class).findById(input.getMoldId()).orElse(null);
		if (mold == null) {
			throw new BizException("DATA_IS_NOT_FOUND", new Property("ID", input.getMoldId()));
		}
		
		String zoneId = LocationUtils.getZoneIdByLocation(mold.getLocation());
		Instant fromInst = DateUtils2.toInstant(input.getTimeValue(), DatePattern.yyyyMMddHH, zoneId);
		Instant beforeInst = fromInst.plus(Duration.ofHours(1));
		
		String datePattern = DatePattern.yyyyMMddHHmm;

		String next = DateUtils2.format(fromInst, datePattern, zoneId);	
		Instant nextInst = DateUtils2.toInstant(next, datePattern, zoneId);
		
		return getRanges(mold,nextInst,zoneId);		
	}
		
	public Ranges getRanges(Mold mold, Instant instant, String zoneId) {
		if (instant == null) {
			return null;
		}

		// 1. Get Week Number
		String week = DateUtils2.format(instant.minus(Duration.ofDays(7)), DatePattern.YYYYww, zoneId);

		// 2. Get Total Shot Count Number
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
		
		// 3. Get Sensor(Counter) Code
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
		
		// 4. Get Acceleration Data 
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
				
				// 5. Calculate Range -- *** Need to Mason's Algorithm ***

			}
		});
				
		// 6. Set Range -- *** Need to Mason's Algorithm ***
		Ranges ranges = new Ranges();
		
		return ranges;
		
	}
	
	
	@Deprecated
	private Ranges getRanges_prev(Mold mold, Instant instant, String zoneId) {
		if (instant == null) {
			return null;
		}
		String week = DateUtils2.format(instant.minus(Duration.ofDays(7)), DatePattern.YYYYww, zoneId);
		Ranges ranges = getRanges_prev(mold, week, zoneId, 0);
		return ranges;
	}

	@Deprecated
	private Ranges getRanges_prev(Mold mold, String week, String zoneId, int depth) {
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
				Ranges ranges = getRanges_prev(mold, prevWeek, zoneId, depth + 1);
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

			double[] totalSc = { 0d };
			double[] totalIt = { 0d };
			double[] totalPt = { 0d };
			double[] totalCt = { 0d };
			double[] totalIp = { 0d };
			double[] totalPp = { 0d };
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
					double devision = ValueUtils.toDouble(accDataList.size(), 0d);
					double multiply = shotCount / devision;

					totalSc[0] = totalSc[0] + shotCount;
					for (DatColProcessInfoOut accData : accDataList) {
						totalIt[0] = totalIt[0] + ValueUtils.toDouble(accData.getInjectionTime(), 0d) * multiply;
						totalPt[0] = totalPt[0] + ValueUtils.toDouble(accData.getPackingTime(), 0d) * multiply;
						totalCt[0] = totalCt[0] + ValueUtils.toDouble(accData.getCoolingTime(), 0d) * multiply;
						totalIp[0] = totalIp[0] + ValueUtils.toDouble(accData.getInjectionPressureIndex(), 0d) * multiply;
						totalPp[0] = totalPp[0] + ValueUtils.toDouble(accData.getPackingPressureIndex(), 0d) * multiply;
					}
				}
			});

			double meanIt = totalIt[0] == 0d ? 0d : (totalIt[0] / totalSc[0]);
			double meanPt = totalPt[0] == 0d ? 0d : (totalPt[0] / totalSc[0]);
			double meanCt = totalCt[0] == 0d ? 0d : (totalCt[0] / totalSc[0]);
			double meanIp = totalIp[0] == 0d ? 0d : (totalIp[0] / totalSc[0]);
			double meanPp = totalPp[0] == 0d ? 0d : (totalPp[0] / totalSc[0]);

//			double[] totalNit = { 0d };
//			double[] totalNpt = { 0d };
//			double[] totalNct = { 0d };
//			double[] totalNip = { 0d };
//			double[] totalNpp = { 0d };

			double itLimit = limit(meanIt);
			double ptLimit = limit(meanPt);
			double ctLimit = limit(meanCt);
			double ipLimit = limit(meanIp);
			double ppL1 = limit(meanPp);

			Ranges ranges = new Ranges();
			ranges.itRange = Pair.of(Math.max(0d, meanIt - itLimit), meanIt + itLimit);
			ranges.meanIt = meanIt;
			ranges.ptRange = Pair.of(Math.max(0d, meanPt - ptLimit), meanPt + ptLimit);
			ranges.meanPt = meanPt;
			ranges.ctRange = Pair.of(Math.max(0d, meanCt - ctLimit), meanCt + ctLimit);
			ranges.meanCt = meanCt;
			ranges.ipRange = Pair.of(Math.max(0d, meanIp - ipLimit), meanIp + ipLimit);
			ranges.meanIp = meanIp;
			ranges.ppRange = Pair.of(Math.max(0d, meanPp - ppL1), meanPp + ppL1);
			ranges.meanPp = meanPp;
			return ranges;
		});
		return values;
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
	
	private static double limit(double value) {
		return value == 0d ? 0d : (2d + Math.min(value / 5d, 10d));
	}
	
	public static Ranges emptyRanges() {
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

	public static class Ranges {
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
}
