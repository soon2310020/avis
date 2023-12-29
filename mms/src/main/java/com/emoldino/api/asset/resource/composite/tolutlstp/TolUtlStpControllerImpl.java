package com.emoldino.api.asset.resource.composite.tolutlstp;

import org.springframework.web.bind.annotation.RestController;

import com.emoldino.api.asset.resource.base.mold.util.MoldUtils;
import com.emoldino.api.common.resource.base.option.dto.UtilizationConfig;
import com.emoldino.api.common.resource.base.option.service.OptionService;
import com.emoldino.framework.dto.SuccessOut;
import com.emoldino.framework.util.BeanUtils;

@RestController
public class TolUtlStpControllerImpl implements TolUtlStpController {

	@Override
	public UtilizationConfig get() {
		return MoldUtils.getUtilizationConfig();
	}

	@Override
	public SuccessOut post(UtilizationConfig input) {
		BeanUtils.get(OptionService.class).saveContent("MOLD_UTILIZATION", input);
		return SuccessOut.getDefault();
	}

}
