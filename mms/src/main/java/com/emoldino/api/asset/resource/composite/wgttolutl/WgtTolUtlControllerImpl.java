package com.emoldino.api.asset.resource.composite.wgttolutl;

import org.springframework.web.bind.annotation.RestController;

import com.emoldino.api.asset.resource.composite.wgttolutl.dto.WgtTolUtlGetIn;
import com.emoldino.api.asset.resource.composite.wgttolutl.dto.WgtTolUtlGetOut;
import com.emoldino.api.asset.resource.composite.wgttolutl.service.WgtTolUtlService;
import com.emoldino.framework.util.BeanUtils;

@RestController
public class WgtTolUtlControllerImpl implements WgtTolUtlController {

	@Override
	public WgtTolUtlGetOut get(WgtTolUtlGetIn input) {
		return BeanUtils.get(WgtTolUtlService.class).get(input);
	}

}
