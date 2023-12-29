package com.emoldino.api.supplychain.resource.composite.cappln.dto;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class CapPlnDetailsGetIn extends CapPlnIn {
	private Long partId;
	private Long supplierId;
}
