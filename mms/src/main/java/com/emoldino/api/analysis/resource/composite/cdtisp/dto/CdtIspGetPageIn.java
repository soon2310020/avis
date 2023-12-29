package com.emoldino.api.analysis.resource.composite.cdtisp.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class CdtIspGetPageIn {
	@ApiModelProperty(value = "Log Transfer ID", example = "1")
	private Long id;
	@ApiModelProperty(value = "Time Zone ID")
	private String zoneId;
//	@ApiModelProperty(value = "Whether Checking 1970 or not")
//	private boolean check1970;
//	@ApiModelProperty(value = "Whether Checking 2036 or not")
//	private boolean check2036;
	@ApiModelProperty(value = "Filter of Sensor ID")
	private String ci;
	@ApiModelProperty(value = "Filter of Sensor ID Prefix")
	private String ciStartsWith;
	@ApiModelProperty(value = "Filter of Terminal ID")
	private String ti;
	@ApiModelProperty(value = "Filter of Terminal ID Prefix")
	private String tiStartsWith;
	private String moldCode;
//	@ApiModelProperty(value = "Filter of Received Time")
//	private String rtStartsWith;
//	@ApiModelProperty(value = "Filter of Last Shot Time")
//	private String lstStartsWith;
	@ApiModelProperty(value = "Filter of Time")
	private String timeStartsWith;
	private String timeFrom;
	private String timeTo;
}
