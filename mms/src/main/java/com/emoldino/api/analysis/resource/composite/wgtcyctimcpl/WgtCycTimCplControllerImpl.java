package com.emoldino.api.analysis.resource.composite.wgtcyctimcpl;

import org.springframework.web.bind.annotation.RestController;

import com.emoldino.api.analysis.resource.composite.wgtcyctimcpl.dto.WgtCycTimCplGetIn;
import com.emoldino.api.analysis.resource.composite.wgtcyctimcpl.dto.WgtCycTimCplGetOut;
import com.emoldino.api.analysis.resource.composite.wgtcyctimcpl.service.WgtCycTimCplService;
import com.emoldino.framework.util.BeanUtils;

@RestController
public class WgtCycTimCplControllerImpl implements WgtCycTimCplController {

	@Override
	public WgtCycTimCplGetOut get(WgtCycTimCplGetIn input) {
		return BeanUtils.get(WgtCycTimCplService.class).get(input);
	}

}
