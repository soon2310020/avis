package com.emoldino.api.analysis.resource.composite.datcol.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DatColPostIn {
	private String requestId;

	private String brokerId;
	private String brokerType;
	private String brokerSwVersion;
	private String brokerTime;
	private String dataType;
	private String data;
	private List<DatColPostItem> content;
}
