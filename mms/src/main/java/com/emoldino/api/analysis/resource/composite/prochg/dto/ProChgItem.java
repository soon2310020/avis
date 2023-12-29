package com.emoldino.api.analysis.resource.composite.prochg.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProChgItem {
	private Long moldId;
	private String moldCode;

	private String dateHourRange;
	private Long procChgCount;

	private Long supplierId;
	private String supplierName;
	private Long locationId;
	private String locationName;
}
