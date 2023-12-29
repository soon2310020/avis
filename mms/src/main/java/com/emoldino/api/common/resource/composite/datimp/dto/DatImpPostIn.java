package com.emoldino.api.common.resource.composite.datimp.dto;

import java.util.List;

import com.emoldino.api.common.resource.base.file.dto.FileDto;
import com.emoldino.api.common.resource.composite.datimp.enumeration.DatImpResourceType;

import lombok.Data;

@Data
public class DatImpPostIn {
	private List<DatImpResourceType> resourceType;
	private List<FileDto> fileRefs;
	private boolean overwriteExistingData;
}
