package com.emoldino.api.common.resource.composite.flt.dto;

import com.emoldino.api.common.resource.base.option.enumeration.MasterFilterResourceType;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(Include.NON_NULL)
public class FltResourceType {
	private MasterFilterResourceType resourceType;
	private String resourceUrl;

	public String getName() {
		return resourceType == null ? null : resourceType.name().toLowerCase();
	}
}
