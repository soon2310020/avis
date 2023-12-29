package com.emoldino.api.analysis.resource.composite.cyctimflu.dto;

import java.util.List;

import com.emoldino.framework.dto.TimeSetting;

import io.swagger.annotations.ApiParam;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class CycTimFluGetIn extends TimeSetting {
	private String filterCode;

	@ApiParam(hidden = true, defaultValue = "true")
	private List<Long> supplierId;//previous filter
	/*
	private Long partId;
	*/
}
