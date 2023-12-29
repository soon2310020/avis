package com.emoldino.api.analysis.resource.composite.cyctimdev.dto;

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
public class CycTimDevDetailsGetIn extends TimeSetting {
	private String filterCode;
	private Long supplierId;

//	private Long partId;
	@ApiParam(hidden = true, defaultValue = "true")
	private List<Long> moldId;
}
