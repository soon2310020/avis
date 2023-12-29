package com.emoldino.api.asset.resource.composite.toladt;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.RestController;

import com.emoldino.api.asset.resource.composite.toladt.dto.TolAdtGetIn;
import com.emoldino.api.asset.resource.composite.toladt.dto.TolAdtGetOut;
import com.emoldino.api.asset.resource.composite.toladt.dto.TolAdtRelocationHistoriesGetIn;
import com.emoldino.api.asset.resource.composite.toladt.dto.TolAdtRelocationHistoriesGetOut;
import com.emoldino.api.asset.resource.composite.toladt.service.TolAdtService;

@RestController
public class TolAdtControllerImpl implements TolAdtController {
	@Autowired
	private TolAdtService service;

	@Override
	public TolAdtGetOut get(TolAdtGetIn input, Pageable pageable) {
		return service.get(input, pageable);
	}

	@Override
	public TolAdtRelocationHistoriesGetOut getRelocationHistories(Long moldId, TolAdtRelocationHistoriesGetIn input, Pageable pageable) {
		return service.getRelocationHistories(moldId, input, pageable);
	}

}
