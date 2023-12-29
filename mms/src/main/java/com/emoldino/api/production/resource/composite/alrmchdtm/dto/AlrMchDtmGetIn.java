package com.emoldino.api.production.resource.composite.alrmchdtm.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AlrMchDtmGetIn {
	private String query;
	private String filterCode;
	private String tabName;
	private List<Long> id;
}
