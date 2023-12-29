package com.emoldino.api.analysis.resource.composite.toldat.service.chart;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import com.emoldino.api.analysis.resource.base.data.repository.dataacceleration.DataAccelerationRepository;
import com.emoldino.api.analysis.resource.base.data.repository.datacounter.DataCounter.CycleTime;
import com.emoldino.api.analysis.resource.base.data.repository.datacounter.DataCounterRepository;
import com.emoldino.api.analysis.resource.composite.toldat.dto.TolDatGetChartsOut;
import com.emoldino.api.analysis.resource.composite.toldat.dto.TolDatGetChartsOut.TolDatChartItem;
import com.emoldino.api.analysis.resource.composite.toldat.dto.TolDatGetChartsOut.TolDatChartItem2;
import com.emoldino.api.analysis.resource.composite.toldat.dto.TolDatGetIn;
import com.emoldino.api.analysis.resource.composite.toldat.util.TolDatUtils;
import com.emoldino.api.common.resource.base.location.util.LocationUtils;
import com.emoldino.framework.dto.TimeSetting;
import com.emoldino.framework.enumeration.TimeScale;
import com.emoldino.framework.repository.Q;
import com.emoldino.framework.util.BeanUtils;
import com.emoldino.framework.util.DataUtils;
import com.emoldino.framework.util.DateUtils2;
import com.emoldino.framework.util.DateUtils2.DatePattern;
import com.emoldino.framework.util.DateUtils2.Zone;
import com.emoldino.framework.util.ResourceUtils;
import com.emoldino.framework.util.ValueUtils;
import com.querydsl.core.BooleanBuilder;

import saleson.api.mold.MoldRepository;
import saleson.model.Mold;

@Service
@Transactional
public class TolDatChartService {
	private static final List<TimeScale> TIME_SCALE_SUPPORTED = Arrays.asList(TimeScale.HOUR);

	public TolDatGetChartsOut get(TolDatGetIn input, TimeSetting timeSetting) {
		if (input.isMock()) {
			return ResourceUtils.toRequiredType("classpath:" + StringUtils.replace(TolDatChartService.class.getPackageName(), ".", "/") + "/mock/get.json", TolDatGetChartsOut.class);
		}

		TolDatUtils.adjustAndCheck(input, timeSetting, TIME_SCALE_SUPPORTED);

		Mold mold = BeanUtils.get(MoldRepository.class).findById(input.getMoldId()).orElse(null);
		if (mold == null) {
			return new TolDatGetChartsOut();
		}
		String sensorCode = mold.getCounterCode();
		if (ObjectUtils.isEmpty(sensorCode)) {
			return new TolDatGetChartsOut();
		}

		TolDatGetChartsOut output = new TolDatGetChartsOut();

		String zoneId = LocationUtils.getZoneIdByLocation(mold.getLocation());
		Pair<Instant, Instant> timeRange = TolDatUtils.getTimeRange(timeSetting, zoneId);
		String fromTimeStr = DateUtils2.format(timeRange.getFirst(), DatePattern.yyyyMMddHHmmss, Zone.GMT);
		String toTimeStr = DateUtils2.format(timeRange.getSecond(), DatePattern.yyyyMMddHHmmss, Zone.GMT);

		List<Long> dataIds = new ArrayList<>();
		DataUtils.runEach(DataCounterRepository.class, //
				new BooleanBuilder()//
						.and(Q.dataCounter.counterId.eq(sensorCode))//
						.and(Q.dataCounter.shotEndTime.isNotNull())//
						.and(Q.dataCounter.shotEndTime.isNotEmpty())//
						.and(Q.dataCounter.shotEndTime.goe(fromTimeStr))//
						.and(Q.dataCounter.shotStartTime.isNotNull())//
						.and(Q.dataCounter.shotStartTime.isNotEmpty())//
						.and(Q.dataCounter.shotStartTime.lt(toTimeStr)), //
				Sort.by("shotStartTime"), 100, true, //
				dataCnt -> {
					dataIds.add(dataCnt.getDataId());

					if (ObjectUtils.isEmpty(dataCnt.getCycleTimes())) {
						return;
					}

					Instant shotStartInst = DateUtils2.toInstant(dataCnt.getShotStartTime(), DatePattern.yyyyMMddHHmmss, Zone.GMT);
					Instant instant;

					instant = shotStartInst;
					for (CycleTime cycleTime : dataCnt.getCycleTimes()) {
						String x = DateUtils2.format(instant, "mm:ss", zoneId);
						double y = cycleTime == null ? 0d : cycleTime.getCycleTime().doubleValue();
						if (instant.compareTo(timeRange.getFirst()) >= 0 && instant.compareTo(timeRange.getSecond()) < 0) {
							output.getCycleTimeChart().addItem(new TolDatChartItem().setX(x).setY(y));
						}
						if (y > 0) {
							instant = instant.plus(Duration.ofMillis(ValueUtils.toLong(y * 1000, 0L)));
						}
					}

					instant = shotStartInst;
					if (!ObjectUtils.isEmpty(dataCnt.getTemperature())) {
						String temps = dataCnt.getTemperature();
						int len = temps.length();
						for (int j = 0; j < len; j += 4) {
							if (instant.compareTo(timeRange.getFirst()) >= 0 && instant.compareTo(timeRange.getSecond()) < 0) {
								String x = DateUtils2.format(instant, "mm:ss", zoneId);
								String tempStr = temps.substring(j, j + 4);
								if (!ValueUtils.isNumber(tempStr)) {
									continue;
								}
								double y = ValueUtils.toDouble(tempStr, 0d) / 10d;
								output.getTemperatureChart().addItem(new TolDatChartItem().setX(x).setY(y));
							}
							instant = instant.plus(Duration.ofMinutes(10));
						}
					}
				});

		if (!dataIds.isEmpty()) {
			BeanUtils.get(DataAccelerationRepository.class).findAll(//
					new BooleanBuilder()//
							.and(Q.dataAcceleration.dataId.in(dataIds))//
							.and(Q.dataAcceleration.measurementDate.isNotNull())//
							.and(Q.dataAcceleration.measurementDate.isNotEmpty())//
							.and(Q.dataAcceleration.measurementDate.goe(fromTimeStr))//
							.and(Q.dataAcceleration.measurementDate.isNotNull())//
							.and(Q.dataAcceleration.measurementDate.isNotEmpty())//
							.and(Q.dataAcceleration.measurementDate.lt(toTimeStr))) //
					.forEach(dataAcc -> {
						if (ObjectUtils.isEmpty(dataAcc.getAccelerations())) {
							return;
						}
						Instant instant = DateUtils2.toInstant(dataAcc.getMeasurementDate(), DatePattern.yyyyMMddHHmmss, Zone.GMT);
						if (instant.compareTo(timeRange.getFirst()) < 0 || instant.compareTo(timeRange.getSecond()) >= 0) {
							return;
						}
						String x = DateUtils2.format(instant, "mm:ss", zoneId);
						TolDatChartItem2 item = new TolDatChartItem2().setX(x);
						output.getAccelerationChart().addItem(item);
						dataAcc.getAccelerations().forEach(acc -> {
							if (!ValueUtils.isNumber(acc.getTime())) {
								return;
							}
							item.addY(ValueUtils.toDouble(acc.getTime(), 0d));
						});
					});
		}

		return output;
	}

}
