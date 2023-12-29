package com.emoldino.api.analysis.resource.composite.toldat;

import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.RestController;

import com.emoldino.api.analysis.resource.composite.toldat.dto.TolDatGetIn;
import com.emoldino.api.analysis.resource.composite.toldat.dto.TolDatGetOut;
import com.emoldino.api.analysis.resource.composite.toldat.service.TolDatService;
import com.emoldino.framework.dto.BatchIn;
import com.emoldino.framework.dto.TimeSetting;

@RestController
public class TolDatControllerImpl implements TolDatController {
	@Autowired
	private TolDatService service;

	@Override
	public TolDatGetOut get(TolDatGetIn input, TimeSetting timeSetting, Pageable pageable) {
		return service.get(input, timeSetting, pageable);
	}

//	@Override
//	public TolDatGetChartsOut getCharts(TolDatGetIn input, TimeSetting timeSetting) {
//		// TODO Auto-generated method stub
//		return BeanUtils.get(TolDatChartService.class).get(input, timeSetting);
//	}

	@Override
	public void export(TolDatGetIn input, TimeSetting timeSetting, BatchIn batchin, HttpServletResponse response) {
		service.export(input, timeSetting, batchin, response);
	}

}
