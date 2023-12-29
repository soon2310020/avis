package com.emoldino.api.supplychain.resource.composite.cappln.dto;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import com.emoldino.api.common.resource.composite.flt.dto.FltCompany;
import com.emoldino.framework.dto.TimeSetting;
import com.emoldino.framework.util.ValueUtils;
import com.querydsl.core.annotations.QueryProjection;

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
public class CapPlnDetailsGetOut extends TimeSetting {
	private Long partId;
	private String partName;
	private String partCode;
	private List<FltCompany> suppliers;
	private long prodDemand;

	private List<CapPlnDetails> content = new ArrayList<>();

	public void add(CapPlnDetails item) {
		content.add(item);
	}

	public long getTotalElements() {
		return content == null ? 0 : content.size();
	}

	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	@Builder
	public static class CapPlnDetails {
		private Long moldId;
		private String moldCode;
		private BigDecimal weightedAvgCycleTime;
		private BigDecimal avgCycleTime;
		private Long prodQty;
		private Long prodCapa;

		@QueryProjection
		public CapPlnDetails(Long moldId, String moldCode, Double weightedAvgCycleTime, Double avgCycleTime, Long prodQty, Long prodCapa) {
			this.moldId = moldId;
			this.moldCode = moldCode;
			this.weightedAvgCycleTime = ValueUtils.toBigDecimal(weightedAvgCycleTime, 0d);
			this.avgCycleTime = ValueUtils.toBigDecimal(avgCycleTime, 0d);
			this.prodQty = prodQty;
			this.prodCapa = prodCapa;
		}
	}
}
