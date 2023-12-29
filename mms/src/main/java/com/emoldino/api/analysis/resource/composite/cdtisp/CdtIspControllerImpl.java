package com.emoldino.api.analysis.resource.composite.cdtisp;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.RestController;

import com.emoldino.api.analysis.resource.composite.cdtisp.dto.CdtIspAdjustIn;
import com.emoldino.api.analysis.resource.composite.cdtisp.dto.CdtIspGetPageIn;
import com.emoldino.api.analysis.resource.composite.cdtisp.dto.CdtIspHourly;
import com.emoldino.api.analysis.resource.composite.cdtisp.dto.CdtIspItem;
import com.emoldino.api.analysis.resource.composite.cdtisp.dto.CdtIspPostIn;
import com.emoldino.api.analysis.resource.composite.cdtisp.service.CdtIspService;
import com.emoldino.api.analysis.resource.composite.cdtisp.service.adjust.CdtIspAdjust1stGenDataService;
import com.emoldino.api.analysis.resource.composite.cdtisp.service.statistics.CdtIspStatisticsService;
import com.emoldino.framework.dto.ListOut;
import com.emoldino.framework.dto.SuccessOut;
import com.emoldino.framework.util.BeanUtils;

@RestController
public class CdtIspControllerImpl implements CdtIspController {
	@Autowired
	private CdtIspService service;

	@Override
	public Page<CdtIspItem> getPage(CdtIspGetPageIn input, Pageable pageable) {
		return service.getPage(input, pageable);
	}

	@Override
	public SuccessOut post(CdtIspPostIn input) {
		service.post(input);
		return SuccessOut.getDefault();
	}

	@Override
	public ListOut<CdtIspHourly> getStatisticsPage(CdtIspGetPageIn input, Pageable pageable) {
		return BeanUtils.get(CdtIspStatisticsService.class).getPage(input, pageable);
	}

	@Override
	public SuccessOut adjust1stGenData(CdtIspAdjustIn input) {
		BeanUtils.get(CdtIspAdjust1stGenDataService.class).adjustBatch(input);
		return SuccessOut.getDefault();
	}

	@Override
	public SuccessOut adjust1stGenLog(CdtIspAdjustIn input) {
		BeanUtils.get(CdtIspAdjust1stGenDataService.class).adjustLogBatch(input);
		return SuccessOut.getDefault();
	}

}
