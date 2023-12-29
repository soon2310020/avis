package com.emoldino.api.analysis.resource.composite.prochg.util;

import java.util.Arrays;
import java.util.List;

import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import com.emoldino.framework.enumeration.TimeScale;

public class ProChgUtils {

	public static final List<TimeScale> TIME_SCALE_SUPPORTED = Arrays.asList(TimeScale.YEAR, TimeScale.HALF, TimeScale.QUARTER, TimeScale.MONTH, TimeScale.WEEK, TimeScale.CUSTOM);

	public static final List<TimeScale> BY_MONTH_SCALES = Arrays.asList(TimeScale.YEAR, TimeScale.HALF, TimeScale.QUARTER);

	public static final List<TimeScale> BY_DAY_SCALES = Arrays.asList(TimeScale.MONTH, TimeScale.WEEK, TimeScale.CUSTOM);

	public static String toHour(String hour) {
		if (ObjectUtils.isEmpty(hour) || hour.length() < 16) {
			return hour;
		}
		hour = StringUtils.replace(hour, "-", "");
		hour = StringUtils.replace(hour, " ", "");
		hour = hour.substring(0, 10);
		return hour;
	}

}
