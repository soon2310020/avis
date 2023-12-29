package com.emoldino.api.asset.resource.composite.toleol;

import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.RestController;

import com.emoldino.api.asset.resource.composite.toleol.dto.TolEolGetIn;
import com.emoldino.api.asset.resource.composite.toleol.dto.TolEolGetOut;
import com.emoldino.api.asset.resource.composite.toleol.service.TolEolService;
import com.emoldino.framework.util.BeanUtils;

@RestController
public class TolEolControllerImpl implements TolEolController {

	@Override
	public TolEolGetOut get(TolEolGetIn input, Pageable pageable) {
		return BeanUtils.get(TolEolService.class).get(input, pageable);
	}

}
