package com.emoldino.api.integration.resource.composite.pqwrk.dto;

import java.util.List;

import lombok.Data;

@Data
public class AiPqResultFields {
	private Long moldId;
	private List<Long> statisticsId;	
	private List<String> ctStatus;
	private List<String> tempStatus;
	private List<String> ppaStatus;
	private List<String> qdStatus;
}
