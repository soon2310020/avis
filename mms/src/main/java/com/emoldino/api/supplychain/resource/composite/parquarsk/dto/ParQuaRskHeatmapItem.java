package com.emoldino.api.supplychain.resource.composite.parquarsk.dto;

import com.emoldino.framework.util.ValueUtils;
import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import saleson.common.enumeration.PriorityType;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ParQuaRskHeatmapItem {
	@JsonIgnore
	private String timeValue;
	private String date;
	private String hour;
	private Integer prodQty;
	@JsonIgnore
	private String ppaStatus;

	public ParQuaRskHeatmapItem(String timeValue, Integer prodQty, String ppaStatus) {
		this.timeValue = timeValue;
		this.prodQty = prodQty;
		this.ppaStatus = ppaStatus;
	}

	public Integer getProdQty() {
		return ValueUtils.toInteger(prodQty, 0);
	}

	public PriorityType getRiskLevel() {
		if (prodQty == null || prodQty <= 0 || "NO_PRODUCTION".equals(ppaStatus)) {
			return null;
		} else if (ppaStatus == null || "STABLE".equals(ppaStatus)) {
			return PriorityType.LOW;
		} else if ("UNSTABLE_CT".equals(ppaStatus)) {
			return PriorityType.MEDIUM;
		} else {
			return PriorityType.HIGH;
		}
	}

	public String getDescr() {
		if (prodQty == null || prodQty <= 0 || "NO_PRODUCTION".equals(ppaStatus)) {
			return "-";
		} else if (ppaStatus == null || "STABLE".equals(ppaStatus)) {
			return "Stable Production";
		} else if ("UNSTABLE_CT".equals(ppaStatus)) {
			return "Production with unstable cycle time";
		} else if ("WARM_UP".equals(ppaStatus)) {
			return "Production during warm-up time";
		} else if ("COOL_DOWN".equals(ppaStatus)) {
			return "Production during cool-down time";
		}
		return "-";
	}
}
