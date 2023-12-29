package com.emoldino.api.asset.resource.composite.alreol.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import saleson.common.enumeration.SpecialAlertType;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AlrEolGetIn {
	private String query;
	private String filterCode;
	private String tabName;
	private List<Long> id;

	private SpecialAlertType specialAlertType;
}
