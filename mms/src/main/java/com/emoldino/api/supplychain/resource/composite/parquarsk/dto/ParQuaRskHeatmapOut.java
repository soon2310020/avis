package com.emoldino.api.supplychain.resource.composite.parquarsk.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ParQuaRskHeatmapOut {
	private List<ParQuaRskHeatmapItem> heatmapItems;
}
