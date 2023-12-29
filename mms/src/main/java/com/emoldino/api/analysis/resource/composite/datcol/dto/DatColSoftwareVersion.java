package com.emoldino.api.analysis.resource.composite.datcol.dto;

import java.util.List;

import org.springframework.util.ObjectUtils;

import com.emoldino.api.common.resource.base.file.annotation.FileRelation;
import com.emoldino.api.common.resource.base.file.dto.FileDto;

import lombok.Data;

@Data
public class DatColSoftwareVersion {
	private String deviceType;
	private String version;
	private Long versionNo;

	@FileRelation(prefix = "MNG", paramName = "file", field = { "value:SOFTWARE", "deviceType" }, versionField = "version")
	private List<FileDto> fileRefs;

	public String getReleaseType() {
		if (ObjectUtils.isEmpty(version)) {
			return "NONE";
		}
		return version.contains("-RC") || version.contains(".RC") ? "CANDIDATE" : "OFFICIAL";
	}
}
