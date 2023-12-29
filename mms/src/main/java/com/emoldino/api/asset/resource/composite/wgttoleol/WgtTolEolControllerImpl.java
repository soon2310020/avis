package com.emoldino.api.asset.resource.composite.wgttoleol;

import org.springframework.web.bind.annotation.RestController;

import com.emoldino.api.asset.resource.composite.wgttoleol.dto.WgtTolEolGetIn;
import com.emoldino.api.asset.resource.composite.wgttoleol.dto.WgtTolEolGetOut;
import com.emoldino.api.asset.resource.composite.wgttoleol.service.WgtTolEolService;
import com.emoldino.framework.util.BeanUtils;

@RestController
public class WgtTolEolControllerImpl implements WgtTolEolController {

	@Override
	public WgtTolEolGetOut get(WgtTolEolGetIn input) {
		return BeanUtils.get(WgtTolEolService.class).get(input);
	}

}
