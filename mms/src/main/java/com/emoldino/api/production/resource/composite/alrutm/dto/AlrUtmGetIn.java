package com.emoldino.api.production.resource.composite.alrutm.dto;

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
public class AlrUtmGetIn {
	private String query;
	private String filterCode;
	private String tabName;
	private SpecialAlertType specialAlertType;
	private List<Long> id;
}
