package com.emoldino.api.asset.resource.composite.toladt.dto;

import com.emoldino.api.asset.resource.base.mold.enumeration.ToolingStatus;
import com.emoldino.api.asset.resource.base.mold.enumeration.ToolingUtilizationStatus;
import com.emoldino.api.asset.resource.composite.toladt.enumeration.TolAdtAreaType;
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
public class TolAdtGetIn extends TimeSetting {
	private String query;
	private Long locationId;
	private ToolingUtilizationStatus utilizationStatus;
	private ToolingStatus toolingStatus;
	private TolAdtAreaType areaType;

	public String getFilterCode() {
		return "COMMON";
	}
}
