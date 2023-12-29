package com.emoldino.api.analysis.resource.base.data.util;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.util.ObjectUtils;

import com.emoldino.api.analysis.resource.base.data.repository.datacounter.DataCounter;
import com.emoldino.api.analysis.resource.base.data.repository.datacounter.DataCounter.CycleTime;
import com.emoldino.framework.util.DateUtils2;
import com.emoldino.framework.util.DateUtils2.DatePattern;
import com.emoldino.framework.util.DateUtils2.Zone;
import com.emoldino.framework.util.ValueUtils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MoldDataUtils {

	public static boolean isCtIdle(BigDecimal ct) {
		return isCtIdle(ValueUtils.toDouble(ct, null));
	}

	public static boolean isCtIdle(Double ct) {
		return ct == null || ct < 0.001 || 999.8 < ct;
	}

	public static boolean isCtWithin(Double ct, Double ctLl1, Double ctUl1) {
		return ctLl1 != null && ctUl1 != null && !isCtIdle(ct) && ctLl1 < ct && ct < ctUl1;
	}

	public static boolean isCtOutOfL1(BigDecimal ct, BigDecimal ctLl2, BigDecimal ctLl1, BigDecimal ctUl1, BigDecimal ctUl2) {
		return isCtOutOfL1(//
				ValueUtils.toDouble(ct, null), //
				ValueUtils.toDouble(ctLl2, null), //
				ValueUtils.toDouble(ctLl1, null), //
				ValueUtils.toDouble(ctUl1, null), //
				ValueUtils.toDouble(ctUl2, null)//
		);
	}

	public static boolean isCtOutOfL1(Double ct, Double ctLl2, Double ctLl1, Double ctUl1, Double ctUl2) {
		return ctLl1 != null && ctUl1 != null && !isCtIdle(ct) && !isCtOutOfL2(ct, ctLl2, ctUl2) && (ct <= ctLl1 || ctUl1 <= ct);
	}

	public static boolean isCtOutOfL2(BigDecimal ct, BigDecimal ctLl2, BigDecimal ctUl2) {
		return isCtOutOfL2(ValueUtils.toDouble(ct, null), ValueUtils.toDouble(ctLl2, null), ValueUtils.toDouble(ctUl2, null));
	}

	public static boolean isCtOutOfL2(Double ct, Double ctLl2, Double ctUl2) {
		return ctLl2 != null && ctUl2 != null && !isCtIdle(ct) && (ct <= ctLl2 || ctUl2 <= ct);
	}

	public static void populateByTime(DataCounter data, Map<String, List<CycleTime>> shotsTimeMap, Map<String, String> tempsTimeMap, String zoneId) {

		List<String> temps = new ArrayList<>();
		if (tempsTimeMap != null && !ObjectUtils.isEmpty(data.getTemperature())) {
			String tempStr = data.getTemperature();
			for (int i = 0; i < tempStr.length(); i += 4) {
				String tempVal = tempStr.substring(i, i + 4);
				if (!ValueUtils.isNumber(tempVal)) {
					continue;
				}
				temps.add(tempVal);
			}
		}

		if (ObjectUtils.isEmpty(data.getCycleTimes()) || data.getCycleTimes().size() == 1) {
			String shotEndTime = DateUtils2.toOtherZone(data.getShotEndTime(), DatePattern.yyyyMMddHHmmss, Zone.GMT, zoneId);
			if (shotsTimeMap != null) {
				shotsTimeMap.put(shotEndTime, data.getCycleTimes());
			}
			if (tempsTimeMap != null) {
				tempsTimeMap.put(shotEndTime, toTempStr(temps, shotEndTime));
			}
		} else {
			// When ShotStartTime is empty
			if (ObjectUtils.isEmpty(data.getShotStartTime()) && !ObjectUtils.isEmpty(data.getShotEndTime())) {
				long time = 0L;
				for (CycleTime cycleTime : data.getCycleTimes()) {
					double ct = cycleTime == null ? 0d : cycleTime.getCycleTime().doubleValue();
					time += ValueUtils.toLong(ct * 1000, 0L);
				}
				Instant shotEndInst = DateUtils2.toInstant(data.getShotEndTime(), DatePattern.yyyyMMddHHmmss, Zone.GMT);
				Instant shotStartInst = shotEndInst.minus(Duration.ofMillis(time));
				String shotStartTime = DateUtils2.format(shotStartInst, DatePattern.yyyyMMddHHmmss, Zone.GMT);
				data.setShotStartTime(shotStartTime);
			}
			Instant shotStartInst = DateUtils2.toInstant(data.getShotStartTime(), DatePattern.yyyyMMddHHmmss, Zone.GMT);
			Instant instant = DateUtils2.toInstant(data.getShotStartTime(), DatePattern.yyyyMMddHHmmss, Zone.GMT);
			String prevShotEndTime = null;
			List<CycleTime> shotByHourList = new ArrayList<>();
			for (CycleTime shot : data.getCycleTimes()) {
				double ct = shot == null ? 0d : shot.getCycleTime().doubleValue();
				Instant prevInstant = instant;
				if (ct > 0) {
					instant = instant.plus(Duration.ofMillis(ValueUtils.toLong(ct * 1000, 0L)));
				}

				String shotEndTime = DateUtils2.format(instant, DatePattern.yyyyMMddHHmmss, zoneId);
				String hour = shotEndTime.substring(0, 10);
				// Separate Shot by Hour List
				if (prevShotEndTime != null && !prevShotEndTime.startsWith(hour)) {
					if (shotsTimeMap != null) {
						shotsTimeMap.put(prevShotEndTime, shotByHourList);
					}
					if (!ObjectUtils.isEmpty(temps)) {
						List<String> temps2 = new ArrayList<>();
						if (prevInstant.equals(shotStartInst)) {
							temps2.add(temps.remove(0));
						} else {
							long _10mins = Math.max(1L, (prevInstant.getEpochSecond() - shotStartInst.getEpochSecond()) / 600);
							for (int i = 0; i < _10mins; i++) {
								temps2.add(temps.remove(0));
								if (temps.isEmpty()) {
									break;
								}
							}
						}
						tempsTimeMap.put(prevShotEndTime, toTempStr(temps2, prevShotEndTime));
						shotStartInst = instant;
					}
					shotByHourList = new ArrayList<>();
				}
				shotByHourList.add(shot);
				prevShotEndTime = shotEndTime;
			}
			if (shotsTimeMap != null) {
				shotsTimeMap.put(prevShotEndTime, shotByHourList);
			}
			if (tempsTimeMap != null) {
				tempsTimeMap.put(prevShotEndTime, toTempStr(temps, prevShotEndTime));
			}
		}
	}

	private static String toTempStr(List<String> temps, String tff) {
		if (ObjectUtils.isEmpty(temps) || ObjectUtils.isEmpty(tff)) {
			return "";
		}
		StringBuilder buf = new StringBuilder();
		String lastStr = null;
		for (String str : temps) {
			buf.append(str).append("/");
			lastStr = str;
		}
		buf.append(tff).append("/");
		buf.append(lastStr);
		return buf.toString();
	}
}
