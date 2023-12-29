package com.emoldino.api.supplychain.resource.composite.demcpl.dto;

import com.emoldino.api.common.resource.composite.flt.dto.FltPart;
import com.emoldino.framework.util.ValueUtils;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import saleson.common.enumeration.PriorityType;

@Getter
@Setter
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class DemCplPart extends FltPart {
	private PriorityType demandCompliance;
	private double demandComplianceRate;

	public DemCplPart(FltPart part, PriorityType demandCompliance, double demandComplianceRate) {
		ValueUtils.map(part, this);
		this.demandCompliance = demandCompliance;
		this.demandComplianceRate = demandComplianceRate;
	}

	@Deprecated
	public PriorityType getCompLevel() {
		return demandCompliance;
	}
}
