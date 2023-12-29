package com.emoldino.api.supplychain.resource.composite.demcpl.util;

import java.util.Arrays;
import java.util.List;

import com.emoldino.framework.enumeration.TimeScale;

public class DemCplUtils {

	public static final List<TimeScale> TIME_SCALE_SUPPORTED = Arrays.asList(TimeScale.WEEK);

	public static final List<TimeScale> TIME_SCALE_DEMAND_SUPPORTED = Arrays.asList(TimeScale.YEAR);

}
