package com.emoldino.api.analysis.resource.composite.trdsen;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.emoldino.api.analysis.resource.composite.trdsen.dto.TrdSenAccelerationChartGetIn;
import com.emoldino.api.analysis.resource.composite.trdsen.dto.TrdSenAccelerationChartGetOut;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Api(protocols = "http, https", tags = "Analysis / 3rd Generation AIOT Sensor")
@RequestMapping("/api/analysis/trd-sen")
public interface TrdSenController {

	@ApiOperation("Get Acceleration Chart Data")
	@GetMapping("/acceleration-chart")
	TrdSenAccelerationChartGetOut getAccelerationChart(TrdSenAccelerationChartGetIn input);

}
