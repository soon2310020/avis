package com.emoldino.api.analysis.resource.composite.prochg;

import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.RestController;

import com.emoldino.api.analysis.resource.base.data.service.moldprocchg.MoldProcChgService;
import com.emoldino.api.analysis.resource.composite.prochg.dto.ProChgDetailsGetIn;
import com.emoldino.api.analysis.resource.composite.prochg.dto.ProChgDetailsGetOut;
import com.emoldino.api.analysis.resource.composite.prochg.dto.ProChgGetIn;
import com.emoldino.api.analysis.resource.composite.prochg.dto.ProChgGetOut;
import com.emoldino.api.analysis.resource.composite.prochg.service.ProChgService;
import com.emoldino.framework.dto.SuccessOut;
import com.emoldino.framework.dto.TimeSetting;
import com.emoldino.framework.util.BeanUtils;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class ProChgControllerImpl implements ProChgController {

	private final ProChgService service;

	@Override
	public ProChgGetOut get(ProChgGetIn input, TimeSetting timeSetting, Pageable pageable) {
		return service.get(input, timeSetting, pageable);
	}

	@Override
	public ProChgDetailsGetOut getDetails(ProChgDetailsGetIn input, Pageable pageable) {
		return service.getDetails(input, pageable);
	}

	@Override
	public SuccessOut summarizeBatch() {
		BeanUtils.get(MoldProcChgService.class).summarizeBatch();
		return SuccessOut.getDefault();
	}

}
