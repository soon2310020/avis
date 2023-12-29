package com.emoldino.api.analysis.resource.composite.toldat.util;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

import org.springframework.data.util.Pair;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import com.emoldino.api.analysis.resource.composite.toldat.dto.TolDatGetIn;
import com.emoldino.framework.dto.TimeSetting;
import com.emoldino.framework.enumeration.TimeScale;
import com.emoldino.framework.exception.LogicException;
import com.emoldino.framework.util.BeanUtils;
import com.emoldino.framework.util.DataUtils;
import com.emoldino.framework.util.DateUtils2;
import com.emoldino.framework.util.DateUtils2.DatePattern;
import com.emoldino.framework.util.LogicUtils;
import com.emoldino.framework.util.ThreadUtils;
import com.emoldino.framework.util.ValueUtils;

import saleson.api.mold.MoldRepository;
import saleson.model.Mold;

public class TolDatUtils {

	public static void adjustAndCheck(TolDatGetIn input, TimeSetting timeSetting, List<TimeScale> timeScaleSupported) {
		adjust(input, timeSetting);
		ValueUtils.assertNotEmpty(input.getMoldId(), "moldId");
		LogicUtils.assertNotEmpty(timeScaleSupported, "timeScaleSupported");
		ValueUtils.assertTimeSetting(timeSetting, timeScaleSupported);
	}

	public static void adjust(TolDatGetIn input, TimeSetting timeSetting) {
		if (input.getMoldId() == null && !ObjectUtils.isEmpty(input.getMoldCode())) {
			input.setMoldId(TolDatUtils.getMoldId(input.getMoldCode()));
		}
		if (!ObjectUtils.isEmpty(timeSetting.getTimeValue())) {
			timeSetting.setTimeValue(StringUtils.replace(timeSetting.getTimeValue(), "-", ""));
		}
	}

	private static Long getMoldId(String moldCode) {
		Long moldId = (Long) ThreadUtils.getProp("TolDatUtils.moldId." + moldCode, () -> {
			Mold mold = BeanUtils.get(MoldRepository.class).findByEquipmentCode(moldCode);
			if (mold == null) {
				throw DataUtils.newDataNotFoundException(Mold.class, "mold_code", moldCode);
			}
			return mold.getId();
		});
		return moldId;
	}

	public static Pair<Instant, Instant> getTimeRange(TimeSetting timeSetting, String zoneId) {
		LogicUtils.assertNotNull(timeSetting, "timeSetting");
		LogicUtils.assertNotEmpty(zoneId, "zoneId");
		Instant fromInst = null;
		Instant beforeInst = null;
		if (TimeScale.HOUR.equals(timeSetting.getTimeScale())) {
			fromInst = DateUtils2.toInstant(timeSetting.getTimeValue(), DatePattern.yyyyMMddHH, zoneId);
			beforeInst = fromInst.plus(Duration.ofHours(1));
		} else if (TimeScale.DATE.equals(timeSetting.getTimeScale())) {
			fromInst = DateUtils2.toInstant(timeSetting.getTimeValue(), DatePattern.yyyyMMdd, zoneId);
			beforeInst = fromInst.plus(Duration.ofDays(1));
		} else if (TimeScale.WEEK.equals(timeSetting.getTimeScale())) {
			fromInst = DateUtils2.toInstant(timeSetting.getTimeValue(), DatePattern.YYYYww, zoneId);
			beforeInst = fromInst.plus(Duration.ofDays(7));
		} else if (TimeScale.MONTH.equals(timeSetting.getTimeScale())) {
			fromInst = DateUtils2.toInstant(timeSetting.getTimeValue(), DatePattern.yyyyMM, zoneId);
			beforeInst = DateUtils2.plusMonths(fromInst, 1, zoneId);
		} else if (TimeScale.YEAR.equals(timeSetting.getTimeScale())) {
			fromInst = DateUtils2.toInstant(timeSetting.getTimeValue(), DatePattern.yyyy, zoneId);
			beforeInst = DateUtils2.toInstant((ValueUtils.toInteger(timeSetting.getTimeValue(), 0) + 1) + "", DatePattern.yyyy, zoneId);
		} else if (TimeScale.CUSTOM.equals(timeSetting.getTimeScale())) {
			fromInst = DateUtils2.toInstant(timeSetting.getFromDate(), DatePattern.yyyyMMdd, zoneId);
			beforeInst = DateUtils2.toInstant(timeSetting.getToDate(), DatePattern.yyyyMMdd, zoneId).plus(Duration.ofDays(1));
		} else {
			throw new LogicException("NOT_SUPPORTED_TIME_SCALE", "TimeScale: " + timeSetting.getTimeScale());
		}
		return Pair.of(fromInst, beforeInst);
	}

}
