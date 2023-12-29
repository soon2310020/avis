package com.emoldino.api.asset.resource.composite.wgttol;

import org.springframework.web.bind.annotation.RestController;

import com.emoldino.api.asset.resource.base.mold.dto.ToolingStatusSummaryGetIn;
import com.emoldino.api.asset.resource.base.mold.service.MoldService;
import com.emoldino.api.asset.resource.composite.wgttol.dto.WgtTolGetIn;
import com.emoldino.api.asset.resource.composite.wgttol.dto.WgtTolGetOut;
import com.emoldino.framework.util.BeanUtils;
import com.emoldino.framework.util.ValueUtils;

@RestController
public class WgtTolControllerImpl implements WgtTolController {

	@Override
	public WgtTolGetOut get(WgtTolGetIn input) {
		return ValueUtils.map(//
				BeanUtils.get(MoldService.class).getStatusSummary(ValueUtils.map(input, ToolingStatusSummaryGetIn.class)), //
				WgtTolGetOut.class//
		);
	}

}
