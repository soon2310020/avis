package com.emoldino.api.analysis.resource.composite.bud;

import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.RestController;

import com.emoldino.api.analysis.resource.composite.bud.dto.BudDetailsGetIn;
import com.emoldino.api.analysis.resource.composite.bud.dto.BudDetailsGetOut;
import com.emoldino.api.analysis.resource.composite.bud.dto.BudGetIn;
import com.emoldino.api.analysis.resource.composite.bud.dto.BudGetOut;
import com.emoldino.api.analysis.resource.composite.bud.service.BudService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class BudControllerImpl implements BudController {

	private final BudService service;

	@Override
	public BudGetOut get(BudGetIn input, Pageable pageable) {
		return service.get(input, pageable);
	}

	@Override
	public BudDetailsGetOut getDetails(BudDetailsGetIn input, Pageable pageable) {
		return service.getDetails(input, pageable);
	}

}
