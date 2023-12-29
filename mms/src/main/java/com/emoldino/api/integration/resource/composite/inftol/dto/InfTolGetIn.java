package com.emoldino.api.integration.resource.composite.inftol.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InfTolGetIn {
	private String toolingId;
	private String toolingIdContains;
	private String updateDateTimeGoe;
}
