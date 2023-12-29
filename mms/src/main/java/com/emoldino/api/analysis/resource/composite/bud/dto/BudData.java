package com.emoldino.api.analysis.resource.composite.bud.dto;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;

import org.springframework.util.ObjectUtils;

import com.emoldino.api.analysis.resource.composite.bud.util.BudUtils;
import com.emoldino.api.analysis.resource.composite.ovrutl.util.OvrUtlUtils;
import com.emoldino.api.asset.resource.base.mold.enumeration.ToolingUtilizationStatus;
import com.querydsl.core.annotations.QueryProjection;

import lombok.AllArgsConstructor;
import lombok.Data;
import saleson.common.enumeration.CurrencyType;

@Data
@AllArgsConstructor
public class BudData {
	private Long moldId;
	private String moldCode;

	private Long companyId;
	private String companyCode;

	private int accumShotCount;
	private int forcastMaxShot;
	private int utilizationRate;
	@Enumerated(EnumType.STRING)
	private ToolingUtilizationStatus lifeCycleStatus;

	private Double cost; // Cost Of Tooling
	private Double salvage; // Salvage Value	
	@Enumerated(EnumType.STRING)
	private CurrencyType costType;

	private int remainingDays;
	private String year;
	private String half; // 1: 1st half , 2: 2nd half

	@QueryProjection
	public BudData(Long moldId, String moldCode, Long companyId, String companyCode, int accumShotCount, int forcastMaxShot, Double cost, Double salvage, CurrencyType costType,
			int remainingDays) {
		this.moldId = moldId;
		this.moldCode = moldCode;
		this.companyId = companyId;
		this.companyCode = companyCode;
		this.accumShotCount = accumShotCount;
		this.forcastMaxShot = forcastMaxShot;
		this.utilizationRate = OvrUtlUtils.setUtiliztionRate(accumShotCount, forcastMaxShot);
		this.lifeCycleStatus = OvrUtlUtils.setLifeCycleStatus(this.utilizationRate);
		this.cost = this.setCost(cost);
		this.salvage = this.setSalvage(salvage);
		this.costType = this.setCostType(costType);
		this.remainingDays = remainingDays;
		this.year = BudUtils.getYear(remainingDays);
		this.half = BudUtils.getHalf(remainingDays);
	}

	private Double setCost(Double cost) {
		if (ObjectUtils.isEmpty(cost)) {
			return (double) 0;
		}
		return cost;
	}

	private Double setSalvage(Double salvage) {
		if (ObjectUtils.isEmpty(salvage)) {
			return (double) 0;
		}
		return cost;
	}

	private CurrencyType setCostType(CurrencyType costType) {
		if (ObjectUtils.isEmpty(costType)) {
			return CurrencyType.USD;
		}
		return costType;
	}

}
