package com.emoldino.api.asset.resource.composite.toleol.dto;

import com.emoldino.framework.dto.TimeSetting;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import saleson.common.enumeration.PriorityType;

@Getter
@Setter
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class TolEolGetIn extends TimeSetting {
	private PriorityType refurbPriority;

	public String getFilterCode() {
		return "COMMON";
	}
}
