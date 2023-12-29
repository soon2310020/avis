package com.emoldino.api.analysis.resource.composite.trdsen;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

import com.emoldino.api.analysis.resource.composite.trdsen.dto.TrdSenAccelerationChartGetIn;
import com.emoldino.api.analysis.resource.composite.trdsen.dto.TrdSenAccelerationChartGetOut;
import com.emoldino.api.analysis.resource.composite.trdsen.service.TrdSenService;

@RestController
public class TrdSenControllerImpl implements TrdSenController {
	@Autowired
	private TrdSenService service;

	@Override
	public TrdSenAccelerationChartGetOut getAccelerationChart(TrdSenAccelerationChartGetIn input) {
		return service.getAccelerationChart(input);
	}
}
