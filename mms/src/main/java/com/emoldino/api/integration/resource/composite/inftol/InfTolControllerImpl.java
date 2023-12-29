package com.emoldino.api.integration.resource.composite.inftol;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.RestController;

import com.emoldino.api.integration.resource.composite.inftol.dto.InfTolDailySummaryIn;
import com.emoldino.api.integration.resource.composite.inftol.dto.InfTolGetIn;
import com.emoldino.api.integration.resource.composite.inftol.dto.InfTolItem;
import com.emoldino.api.integration.resource.composite.inftol.dto.InfTolPostItem;
import com.emoldino.api.integration.resource.composite.inftol.dto.InfTolSummaryItem;
import com.emoldino.api.integration.resource.composite.inftol.service.InfTolService;
import com.emoldino.framework.dto.SuccessOut;
import com.emoldino.framework.util.BeanUtils;

@RestController
public class InfTolControllerImpl implements InfTolController {

	@Override
	public Page<InfTolItem> get(InfTolGetIn input, Pageable pageable) {
		return BeanUtils.get(InfTolService.class).get(input, pageable);
	}

	@Override
	public InfTolItem get(String toolingId) {
		return BeanUtils.get(InfTolService.class).get(toolingId);
	}

	@Override
	public SuccessOut post(String toolingId, InfTolPostItem tooling) {
		BeanUtils.get(InfTolService.class).post(toolingId, tooling);
		return SuccessOut.getDefault();
	}

	@Override
	public Page<InfTolSummaryItem> getDailySummary(InfTolDailySummaryIn input, Pageable pageable) {
		return BeanUtils.get(InfTolService.class).getDailySummary(input, pageable);
	}

}
