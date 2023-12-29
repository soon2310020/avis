package com.emoldino.api.analysis.resource.composite.datcol.dto;

import lombok.Data;

@Data
public class DatColResourcePlant {
	private String name;
	private String locationCode;
	private String companyCode;
	private String address;
	private String timeZoneId;
	private String memo;
	private boolean enabled;
}
