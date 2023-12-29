package com.emoldino.api.analysis.resource.composite.prdbtn.dto;

import java.util.List;

import com.emoldino.framework.dto.TimeSetting;

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
public class PrdBtnDetailsGetIn extends TimeSetting {
	private String filterCode;
	private Long productId;
	private List<Long> supplierId;
	private Long partId;
}
