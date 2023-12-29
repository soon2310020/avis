package com.emoldino.api.supplychain.resource.composite.demcpl.dto;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class DemCplProductDemandsPostIn {
	private List<DemCplProductDemand> content = new ArrayList<>();

	public void add(DemCplProductDemand item) {
		content.add(item);
	}
}
