package com.emoldino.api.common.resource.composite.datimp.service.deprecated;

import java.util.Map;

import lombok.Data;

@Deprecated
@Data
public abstract class DatImpRawDatBase {
	protected int rowIndex;
	protected Map<String, String> customFields;
}
