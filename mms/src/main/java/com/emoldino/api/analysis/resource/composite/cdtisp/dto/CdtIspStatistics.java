package com.emoldino.api.analysis.resource.composite.cdtisp.dto;

import lombok.Data;

@Data
public class CdtIspStatistics {
	private String tff;
	private String rt;
	private Integer sc;
	private Integer shotCount;
	private Integer shotCountVal;
	private Double ct;
	private Double ctVal;
	private String ctt;
	private String cttVal;

	private Long cdataId;
	private Long transferId;
	private long transferCount;
	private Long statisticsId;
	private String statisticsCreatedAt;
	private long statisticsCount;
}
