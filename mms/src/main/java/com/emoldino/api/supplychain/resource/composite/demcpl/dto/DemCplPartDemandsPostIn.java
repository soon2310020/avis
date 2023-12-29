package com.emoldino.api.supplychain.resource.composite.demcpl.dto;

import java.util.ArrayList;
import java.util.List;

import com.emoldino.api.supplychain.resource.base.product.dto.PartPlanYearly;
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
public class DemCplPartDemandsPostIn extends TimeSetting {
	private List<PartPlanYearly> content = new ArrayList<>();

	public void add(PartPlanYearly item) {
		content.add(item);
	}
}
