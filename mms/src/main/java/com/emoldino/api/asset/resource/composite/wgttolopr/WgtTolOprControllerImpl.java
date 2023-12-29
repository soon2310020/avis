package com.emoldino.api.asset.resource.composite.wgttolopr;

import org.springframework.web.bind.annotation.RestController;

import com.emoldino.api.asset.resource.base.mold.dto.ToolingStatusSummaryGetIn;
import com.emoldino.api.asset.resource.base.mold.service.MoldService;
import com.emoldino.api.asset.resource.composite.wgttolopr.dto.WgtTolOprGetIn;
import com.emoldino.api.asset.resource.composite.wgttolopr.dto.WgtTolOprGetOut;
import com.emoldino.framework.util.BeanUtils;
import com.emoldino.framework.util.ValueUtils;

@RestController
public class WgtTolOprControllerImpl implements WgtTolOprController {

	@Override
	public WgtTolOprGetOut get(WgtTolOprGetIn input) {
		return ValueUtils.map(//
				BeanUtils.get(MoldService.class).getStatusSummary(ValueUtils.map(input, ToolingStatusSummaryGetIn.class)), //
				WgtTolOprGetOut.class//
		);
	}

}
