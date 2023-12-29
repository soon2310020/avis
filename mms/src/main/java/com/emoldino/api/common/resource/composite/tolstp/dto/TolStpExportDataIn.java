package com.emoldino.api.common.resource.composite.tolstp.dto;

import com.emoldino.api.common.resource.base.option.enumeration.MasterFilterMode;
import com.emoldino.api.common.resource.composite.tolstp.enumeration.TolStpDataFrequency;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class TolStpExportDataIn extends TolStpGetIn {
	private TolStpDataFrequency dataFrequency;
	private boolean cycleTimeEnabled;
	private boolean shotCountEnabled;
	private boolean uptimeEnabled;
	private boolean temperatureEnabled;
	@Deprecated
	private MasterFilterMode mode;

	private Integer timezoneOffsetClient;//improve later
}
