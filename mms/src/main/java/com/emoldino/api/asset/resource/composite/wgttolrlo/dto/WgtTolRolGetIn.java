package com.emoldino.api.asset.resource.composite.wgttolrlo.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import saleson.common.enumeration.MoldLocationStatus;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WgtTolRolGetIn {
	private List<MoldLocationStatus> moldLocationStatus;

	public String getFilterCode() {
		return "COMMON";
	}
}
