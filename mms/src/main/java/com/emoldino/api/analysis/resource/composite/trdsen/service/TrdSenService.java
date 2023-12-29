package com.emoldino.api.analysis.resource.composite.trdsen.service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import com.emoldino.api.analysis.resource.base.data.repository.dataacceleration.DataAcceleration;
import com.emoldino.api.analysis.resource.base.data.repository.dataacceleration.DataAccelerationRepository;
import com.emoldino.api.analysis.resource.composite.trdsen.dto.TrdSenAccelerationChartGetIn;
import com.emoldino.api.analysis.resource.composite.trdsen.dto.TrdSenAccelerationChartGetOut;
import com.emoldino.api.analysis.resource.composite.trdsen.dto.TrdSenAccelerationChartGetOut.TrdSenAccelerationChartPoint;
import com.emoldino.api.analysis.resource.composite.trdsen.dto.TrdSenAccelerationChartGetOut.TrdSenAccelerationChartShot;
import com.emoldino.api.common.resource.base.location.util.LocationUtils;
import com.emoldino.framework.util.BeanUtils;
import com.emoldino.framework.util.DateUtils2;
import com.emoldino.framework.util.DateUtils2.DatePattern;
import com.emoldino.framework.util.DateUtils2.Zone;
import com.emoldino.framework.util.ValueUtils;

@Service
public class TrdSenService {
	private static final Map<Integer, String> PREFIX;
	static {
		Map<Integer, String> map = new ConcurrentHashMap<>();
		map.put(1, "1st");
		map.put(2, "2nd");
		map.put(3, "3rd");
		PREFIX = map;
	}

	public TrdSenAccelerationChartGetOut getAccelerationChart(TrdSenAccelerationChartGetIn input) {
		List<DataAcceleration> list = BeanUtils.get(DataAccelerationRepository.class).findAllByMoldIdAndMeasurementTime(input.getMoldId(), input.getFromDate(), input.getToDate());

		String zoneId = LocationUtils.getZoneIdByMoldId(input.getMoldId());

		TrdSenAccelerationChartGetOut output = new TrdSenAccelerationChartGetOut();
		int[] i = { 0 };
		list.forEach(item -> {
			Instant since = DateUtils2.toInstant(item.getMeasurementDate(), DatePattern.yyyyMMddHHmmss, Zone.GMT);

			String title = (PREFIX.containsKey(++i[0]) ? PREFIX.get(i[0]) : (i[0] + "th")) + " Shot";
			List<TrdSenAccelerationChartPoint> points = new ArrayList<>();
			if (!ObjectUtils.isEmpty(item.getAccelerations())) {
				item.getAccelerations().forEach(acc -> {
					TrdSenAccelerationChartPoint point = new TrdSenAccelerationChartPoint();
					point.setX(Double.parseDouble(acc.getTime()));
					point.setY(Double.parseDouble(acc.getValue()));
					// Calc T value
					{
						Instant instant = since == null ? null : since.plusMillis(ValueUtils.toLong(point.getX() * 1000, 0L));
						String t = DateUtils2.format(instant, DatePattern.yyyy_MM_dd_HH_mm_ss, zoneId);
						point.setT(t);
					}
					points.add(point);
				});
			}
			output.addShot(new TrdSenAccelerationChartShot(title, points));
		});

		return output;
	}

}
