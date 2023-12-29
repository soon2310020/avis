package com.emoldino.api.analysis.resource.composite.tolrpt;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.RestController;

import com.emoldino.api.analysis.resource.composite.tolrpt.dto.TolRptGetIn;
import com.emoldino.api.analysis.resource.composite.tolrpt.dto.TolRptItem;
import com.emoldino.api.analysis.resource.composite.tolrpt.service.TolRptService;
import com.emoldino.framework.dto.BatchIn;
import com.emoldino.framework.util.BeanUtils;

@RestController
public class TolRptControllerImpl implements TolRptController {

	public Page<TolRptItem> get(TolRptGetIn input, BatchIn batchin, Pageable pageable) {
		return BeanUtils.get(TolRptService.class).get(input, batchin, pageable);
	}

	public void export(TolRptGetIn input, BatchIn batchin, HttpServletResponse response) throws IOException {
		BeanUtils.get(TolRptService.class).export(input, batchin, response);
	}

}
