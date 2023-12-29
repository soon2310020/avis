package com.emoldino.api.analysis.resource.composite.proana;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.RestController;

import com.emoldino.api.analysis.resource.composite.proana.dto.ProAnaGetIn;
import com.emoldino.api.analysis.resource.composite.proana.dto.ProAnaGetOut;
import com.emoldino.api.analysis.resource.composite.proana.dto.ProAnaMoldsGetIn;
import com.emoldino.api.analysis.resource.composite.proana.dto.ProAnaMoldsItem;
import com.emoldino.api.analysis.resource.composite.proana.service.ProAnaService;
import com.emoldino.api.analysis.resource.composite.proana.service.Range.ProAnaRangeService;
import com.emoldino.api.analysis.resource.composite.proana.service.Range.ProAnaRangeService.Ranges;
import com.emoldino.api.analysis.resource.composite.proana.service.mold.ProAnaMoldService;
import com.emoldino.framework.util.BeanUtils;

@RestController
public class ProAnaControllerImpl implements ProAnaController {
	@Autowired
	private ProAnaService service;

	@Override
	public Page<ProAnaMoldsItem> getMolds(ProAnaMoldsGetIn input, Pageable pageable) {
		return BeanUtils.get(ProAnaMoldService.class).get(input, pageable);
	}

	@Override
	public ProAnaGetOut get(ProAnaGetIn input, Pageable pageable) {
		return service.get(input, pageable);
	}

	@Override
	public Ranges getRange(ProAnaGetIn input) {		
		return BeanUtils.get(ProAnaRangeService.class).getRangesTest(input);
	}

}
