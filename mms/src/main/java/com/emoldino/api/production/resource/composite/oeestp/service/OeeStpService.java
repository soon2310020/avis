package com.emoldino.api.production.resource.composite.oeestp.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.emoldino.api.common.resource.base.option.service.OptionService;
import com.emoldino.api.production.resource.base.oee.enumeration.OEE_OPTION;
import com.emoldino.api.production.resource.base.oee.util.OeeUtils;
import com.emoldino.api.production.resource.composite.oeestp.dto.OeeStpGetOut;
import com.emoldino.api.production.resource.composite.oeestp.dto.OeeStpPostIn;
import com.emoldino.framework.util.BeanUtils;

@Service
@Transactional
public class OeeStpService {

	public OeeStpGetOut get() {
		return OeeStpGetOut.builder()//
				.oeeScore(OeeUtils.getOeeScoreConfig())//
				.oeeMetricAvailability(OeeUtils.getOeeMetricAvailabilityConfig())//
				.oeeMetricPerformance(OeeUtils.getOeeMetricPerformanceConfig())//
				.oeeMetricQuality(OeeUtils.getOeeMetricQualityConfig())//
				.build();
	}

	public void post(OeeStpPostIn input) {
		BeanUtils.get(OptionService.class).saveContent(OEE_OPTION.OEE_SCORE, //
				input.getOeeScore() != null ? input.getOeeScore() : OeeUtils.getOeeScoreConfig());
		BeanUtils.get(OptionService.class).saveContent(OEE_OPTION.OEE_METRIC_AVAILABILITY, //
				input.getOeeMetricAvailability() != null ? input.getOeeMetricAvailability() : OeeUtils.getOeeMetricAvailabilityConfig());
		BeanUtils.get(OptionService.class).saveContent(OEE_OPTION.OEE_METRIC_PERFORMANCE, //
				input.getOeeMetricPerformance() != null ? input.getOeeMetricPerformance() : OeeUtils.getOeeMetricPerformanceConfig());
		BeanUtils.get(OptionService.class).saveContent(OEE_OPTION.OEE_METRIC_QUALITY,
				input.getOeeMetricQuality() != null ? input.getOeeMetricQuality() : OeeUtils.getOeeMetricQualityConfig());
	}

}
