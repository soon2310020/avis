package com.emoldino.api.supplychain.resource.composite.cappln.dto;

import java.util.ArrayList;
import java.util.List;

import com.emoldino.api.common.resource.composite.flt.dto.FltCompany;
import com.emoldino.api.common.resource.composite.flt.dto.FltPlant;
import com.emoldino.api.supplychain.resource.composite.cappln.dto.CapPlnGetOut.CapPlnItem;
import com.emoldino.framework.dto.ListOut;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
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
public class CapPlnGetOut extends ListOut<CapPlnItem> {
	private List<CapPlnChartItem> chartItems = new ArrayList<>();

	public CapPlnGetOut(List<CapPlnItem> content, List<CapPlnChartItem> chartItems) {
		super(content);
		this.chartItems = chartItems;
	}

	public void addChartItem(CapPlnChartItem chartItem) {
		this.chartItems.add(chartItem);
	}

	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	public static class CapPlnItem {
		private Long partId;
		private String partName;
		private String partCode;
		private List<FltCompany> suppliers;
		private List<FltPlant> plants;
		private Long moldCount;
		private long prodDemand;
//		private long requiredQty;
		private long prodQty;
		private long prodCapa;

		@Deprecated
		public long getProductionDemand() {
			return prodDemand;
		}

		@Deprecated
		public long getProducedQuantity() {
			return prodQty;
		}

		@Deprecated
		public long getProductionCapacity() {
			return prodCapa;
		}
	}

	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	@Builder
	public static class CapPlnChartItem {
		private String title;
		private String timeValue;
		private long prodDemand;
		private Long prodQty;
		private Long prodCapa;
		private Long predCapa;
	}
}
