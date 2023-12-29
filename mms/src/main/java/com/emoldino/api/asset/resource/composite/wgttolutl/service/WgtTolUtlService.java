package com.emoldino.api.asset.resource.composite.wgttolutl.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.emoldino.api.asset.resource.base.mold.dto.ToolingStatusSummary;
import com.emoldino.api.asset.resource.base.mold.dto.ToolingStatusSummaryGetIn;
import com.emoldino.api.asset.resource.base.mold.dto.ToolingUtilizationSummaryGetIn;
import com.emoldino.api.asset.resource.base.mold.enumeration.ToolingUtilizationStatus;
import com.emoldino.api.asset.resource.base.mold.service.MoldService;
import com.emoldino.api.asset.resource.base.mold.util.MoldUtils;
import com.emoldino.api.asset.resource.composite.wgttolutl.dto.WgtTolUtlGetIn;
import com.emoldino.api.asset.resource.composite.wgttolutl.dto.WgtTolUtlGetOut;
import com.emoldino.api.common.resource.base.option.dto.UtilizationConfig;
import com.emoldino.framework.util.BeanUtils;
import com.emoldino.framework.util.ValueUtils;

@Service
@Transactional
public class WgtTolUtlService {

	public WgtTolUtlGetOut get(WgtTolUtlGetIn input) {
		WgtTolUtlGetOut output = new WgtTolUtlGetOut();
		UtilizationConfig config = MoldUtils.getUtilizationConfig();
		output.setConfig(config);

		output.setLowStd("0-" + config.getLow() + "%");
		output.setMediumStd(config.getLow() + "-" + config.getMedium() + "%");
		output.setHighStd(config.getMedium() + "-" + config.getHigh() + "%");
		output.setProlongedStd(">" + config.getHigh() + "%");

		ValueUtils.map(//
				BeanUtils.get(MoldService.class).getUtilizationSummary(ValueUtils.map(input, ToolingUtilizationSummaryGetIn.class)), //
				output//
		);

		ValueUtils.map(//
				BeanUtils.get(MoldService.class).getStatusSummary(ValueUtils.map(input, ToolingStatusSummaryGetIn.class)), //
				output//
		);

		// Low
		{
			ToolingStatusSummaryGetIn reqin = ValueUtils.map(input, ToolingStatusSummaryGetIn.class);
			reqin.setUtilizationStatus(ToolingUtilizationStatus.LOW);
			ToolingStatusSummary summary = BeanUtils.get(MoldService.class).getStatusSummary(reqin);
			output.setLowStatusSummary(summary);
		}
		// Medium
		{
			ToolingStatusSummaryGetIn reqin = ValueUtils.map(input, ToolingStatusSummaryGetIn.class);
			reqin.setUtilizationStatus(ToolingUtilizationStatus.MEDIUM);
			ToolingStatusSummary summary = BeanUtils.get(MoldService.class).getStatusSummary(reqin);
			output.setMediumStatusSummary(summary);
		}
		// High
		{
			ToolingStatusSummaryGetIn reqin = ValueUtils.map(input, ToolingStatusSummaryGetIn.class);
			reqin.setUtilizationStatus(ToolingUtilizationStatus.HIGH);
			ToolingStatusSummary summary = BeanUtils.get(MoldService.class).getStatusSummary(reqin);
			output.setHighStatusSummary(summary);
		}
		// Prolonged
		{
			ToolingStatusSummaryGetIn reqin = ValueUtils.map(input, ToolingStatusSummaryGetIn.class);
			reqin.setUtilizationStatus(ToolingUtilizationStatus.PROLONGED);
			ToolingStatusSummary summary = BeanUtils.get(MoldService.class).getStatusSummary(reqin);
			output.setProlongedStatusSummary(summary);
		}

		return output;
	}

}
