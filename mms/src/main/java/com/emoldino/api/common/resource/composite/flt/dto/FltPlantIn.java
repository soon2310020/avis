package com.emoldino.api.common.resource.composite.flt.dto;

import java.util.List;

import com.emoldino.framework.util.ValueUtils;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@NoArgsConstructor
public class FltPlantIn extends FltIn {
	private List<Long> partId;
	private List<Long> supplierId;

	public FltPlantIn(FltIn fltIn) {
		ValueUtils.map(fltIn, this);
	}
}
