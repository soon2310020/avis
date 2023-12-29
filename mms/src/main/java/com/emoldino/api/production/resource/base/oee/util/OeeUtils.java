package com.emoldino.api.production.resource.base.oee.util;

import com.emoldino.api.common.resource.base.option.dto.RiskLevelConfig;
import com.emoldino.api.common.resource.base.option.util.OptionUtils;
import com.emoldino.api.production.resource.base.oee.enumeration.OEE_OPTION;

public class OeeUtils {

	public static RiskLevelConfig getOeeScoreConfig() {
		RiskLevelConfig config = OptionUtils.getContent(OEE_OPTION.OEE_SCORE, RiskLevelConfig.class, //
				RiskLevelConfig.builder().low(100).medium(70).high(50).build());
		return config;
	}

	public static RiskLevelConfig getOeeMetricAvailabilityConfig() {
		RiskLevelConfig config = OptionUtils.getContent(OEE_OPTION.OEE_METRIC_AVAILABILITY, RiskLevelConfig.class, //
				RiskLevelConfig.builder().low(100).medium(70).high(50).build());
		return config;
	}

	public static RiskLevelConfig getOeeMetricPerformanceConfig() {
		RiskLevelConfig config = OptionUtils.getContent(OEE_OPTION.OEE_METRIC_PERFORMANCE, RiskLevelConfig.class, //
				RiskLevelConfig.builder().low(100).medium(70).high(50).build());
		return config;
	}

	public static RiskLevelConfig getOeeMetricQualityConfig() {
		RiskLevelConfig config = OptionUtils.getContent(OEE_OPTION.OEE_METRIC_QUALITY, RiskLevelConfig.class, //
				RiskLevelConfig.builder().low(100).medium(70).high(50).build());
		return config;
	}

}
