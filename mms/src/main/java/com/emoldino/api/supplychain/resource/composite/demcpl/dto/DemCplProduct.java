package com.emoldino.api.supplychain.resource.composite.demcpl.dto;

import com.emoldino.api.common.resource.composite.flt.dto.FltProduct;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import saleson.common.enumeration.PriorityType;

@Getter
@Setter
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class DemCplProduct extends FltProduct {
	private PriorityType demandCompliance;
	private double demandComplianceRate;

	public DemCplProduct(Long id, String name, PriorityType demandCompliance, double demandComplianceRate) {
		super(id, name);
		this.demandCompliance = demandCompliance;
		this.demandComplianceRate = demandComplianceRate;
	}

	@Deprecated
	public PriorityType getCompLevel() {
		return demandCompliance;
	}
}
