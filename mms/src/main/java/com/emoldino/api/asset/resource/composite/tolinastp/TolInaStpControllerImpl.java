package com.emoldino.api.asset.resource.composite.tolinastp;

import org.springframework.web.bind.annotation.RestController;

import com.emoldino.api.asset.resource.base.mold.util.MoldUtils;
import com.emoldino.api.common.resource.base.option.dto.InactiveConfig;
import com.emoldino.api.common.resource.base.option.service.OptionService;
import com.emoldino.framework.dto.SuccessOut;
import com.emoldino.framework.util.BeanUtils;

@RestController
public class TolInaStpControllerImpl implements TolInaStpController {

	@Override
	public InactiveConfig get() {
		return MoldUtils.getInactiveConfig();
	}

	@Override
	public SuccessOut post(InactiveConfig input) {
		BeanUtils.get(OptionService.class).saveContent("MOLD_INACTIVE", input);
		return SuccessOut.getDefault();
	}

}
