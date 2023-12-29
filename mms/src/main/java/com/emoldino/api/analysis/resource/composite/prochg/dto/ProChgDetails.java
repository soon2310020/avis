package com.emoldino.api.analysis.resource.composite.prochg.dto;

import java.util.List;

import com.emoldino.api.common.resource.composite.flt.dto.FltPart;
import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProChgDetails {
	private Long id;
	private String procChgTime;

	private Long locationId;
	private String locationName;

	@JsonIgnore
	private String firstPartName;
	private List<FltPart> parts;

	public ProChgDetails(//
			Long id, String procChgTime, //
			Long locationId, String locationName, //
			String firstPartName//
	) {
		this.id = id;
		this.procChgTime = procChgTime;
		this.locationId = locationId;
		this.locationName = locationName;
		this.firstPartName = firstPartName;
	}
}
