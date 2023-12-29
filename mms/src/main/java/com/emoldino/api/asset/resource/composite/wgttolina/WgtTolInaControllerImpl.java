package com.emoldino.api.asset.resource.composite.wgttolina;

import org.springframework.web.bind.annotation.RestController;

import com.emoldino.api.asset.resource.composite.wgttolina.dto.WgtTolInaGetIn;
import com.emoldino.api.asset.resource.composite.wgttolina.dto.WgtTolInaGetOut;
import com.emoldino.api.asset.resource.composite.wgttolina.service.WgtTolInaService;
import com.emoldino.framework.util.BeanUtils;

@RestController
public class WgtTolInaControllerImpl implements WgtTolInaController {

	@Override
	public WgtTolInaGetOut get(WgtTolInaGetIn input) {
		return BeanUtils.get(WgtTolInaService.class).get(input);
	}

}
