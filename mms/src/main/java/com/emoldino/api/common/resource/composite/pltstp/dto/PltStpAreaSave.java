package com.emoldino.api.common.resource.composite.pltstp.dto;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;

import com.emoldino.api.common.resource.base.location.enumeration.AreaType;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class PltStpAreaSave {
	private String name;
	@Enumerated(EnumType.STRING)
	private AreaType areaType;
}
