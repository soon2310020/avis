package com.emoldino.api.supplychain.resource.composite.cappln.util;

import java.util.Arrays;
import java.util.List;

import org.springframework.util.ObjectUtils;

import com.emoldino.api.supplychain.resource.composite.cappln.dto.CapPlnIn;
import com.emoldino.framework.dto.TimeSetting;
import com.emoldino.framework.enumeration.TimeScale;
import com.emoldino.framework.util.DateUtils2;

public class CapPlnUtils {

	public static List<String> toWeeks(CapPlnIn input) {
		List<String> weeks;
		if (!ObjectUtils.isEmpty(input.getMonth())) {
			weeks = DateUtils2.toWeeks(new TimeSetting(TimeScale.MONTH, input.getMonth(), null, null));
		} else if (!ObjectUtils.isEmpty(input.getWeek())) {
			weeks = Arrays.asList(input.getWeek());
		} else {
			weeks = DateUtils2.toWeeks(input);
		}
		return weeks;
	}

}
