package com.emoldino.api.analysis.resource.composite.datviw.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class DatColGetPageItem {
	private Long id;
	private String requestId;
	private Integer position;

	private String procStatus;
	private Long procErrorId;

	private String occurredAtStr;
	private String sentAtStr;
	private String createdAtStr;
	private String distributedAtStr;
	private String analyzedAtStr;
	private String updatedAtStr;

	private String dataType;
	private String data;

	private String deviceId;
	private String deviceType;
	private String deviceSwVersion;

	private String brokerId;
	private String brokerType;
	private String brokerSwVersion;
}
