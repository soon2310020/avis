package com.emoldino.api.analysis.resource.composite.proana.dto;

import org.springframework.util.ObjectUtils;

import com.emoldino.api.asset.resource.base.mold.enumeration.ToolingStatus;
import com.emoldino.api.common.resource.base.location.util.LocationUtils;
import com.emoldino.framework.util.DateUtils2;
import com.emoldino.framework.util.DateUtils2.DatePattern;
import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import saleson.model.Mold;

@Data
@NoArgsConstructor
public class ProAnaMoldsItem {
	@Getter(AccessLevel.PRIVATE)
	@JsonIgnore
	Mold mold;

	public ProAnaMoldsItem(Mold mold) {
		this.mold = mold;
	}

	public Long getId() {
		return mold == null ? null : mold.getId();
	}

	public String getMoldCode() {
		return mold == null ? null : mold.getEquipmentCode();
	}

	public String getUpdatedDateTime() {
		return mold == null ? null : DateUtils2.format(mold.getUpdatedAt(), DatePattern.yyyy_MM_dd_HH_mm_ss, LocationUtils.getZoneIdByMold(mold));
	}

	public Double getUtilizationRate() {
		return mold == null ? null : mold.getUtilizationRate();
	}

	public Double getApprovedCycleTime() {
		if (mold == null) {
			return null;
		}

		if (!ObjectUtils.isEmpty(mold.getContractedCycleTimeSeconds())) {
			return mold.getContractedCycleTimeSeconds();
		} else {
			return mold.getToolmakerContractedCycleTimeSeconds();
		}
	}

	public Double getAverageCycleTime() {
		return mold == null || mold.getWeightedAverageCycleTime() == null ? null : (mold.getWeightedAverageCycleTime() / 10.0);
	}

	public ToolingStatus getToolingStatus() {
		return mold == null ? null : mold.getToolingStatus();
	}
}
