package com.emoldino.api.analysis.resource.composite.ovrutl;

import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.RestController;

import com.emoldino.api.analysis.resource.composite.ovrutl.dto.OvrUtlDetailsGetIn;
import com.emoldino.api.analysis.resource.composite.ovrutl.dto.OvrUtlDetailsGetOut;
import com.emoldino.api.analysis.resource.composite.ovrutl.dto.OvrUtlGetIn;
import com.emoldino.api.analysis.resource.composite.ovrutl.dto.OvrUtlGetOut;
import com.emoldino.api.analysis.resource.composite.ovrutl.service.OvrUtlService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class OvrUtlControllerImpl implements OvrUtlController {

	private final OvrUtlService service;

	@Override
	public OvrUtlGetOut get(OvrUtlGetIn input, Pageable pageable) {
		return service.get(input, pageable);
	}

	@Override
	public OvrUtlDetailsGetOut getDetails(OvrUtlDetailsGetIn input, Pageable pageable) {
		return service.getDetails(input, pageable);
	}

}
