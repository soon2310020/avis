package com.emoldino.api.common.resource.composite.flemng.dto;

import java.time.Instant;
import java.util.List;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;

import com.emoldino.api.common.resource.base.file.annotation.FileRelation;
import com.emoldino.api.common.resource.base.file.dto.FileDto;
import com.emoldino.api.common.resource.base.file.enumeration.FileGroupStatus;
import com.emoldino.api.common.resource.base.file.enumeration.FileGroupType;
import com.emoldino.api.common.resource.base.file.repository.filegroup.FileGroup;
import com.emoldino.framework.util.ValueUtils;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class FleMngGroup {
	private Long id;

	@Enumerated(EnumType.STRING)
	private FileGroupType fileGroupType;

	@ApiModelProperty(value = "File Group Code", required = true)
	private String fileGroupCode;
	@ApiModelProperty(value = "File Group Name")
	private String fileGroupName;
	@ApiModelProperty(value = "File Group Version", required = true)
	private String version = "0.0.1";
	@ApiModelProperty(value = "Recently Released Version")
	private String releasedVersion;
	@ApiModelProperty(value = "File Group Description")
	private String description;
	@ApiModelProperty(value = "File Group Status - RELEASED/UNRELEASED")
	@Enumerated(EnumType.STRING)
	private FileGroupStatus fileGroupStatus;
	private Instant releasedAt;

	private boolean enabled = true;

	@FileRelation(prefix = "MNG", paramName = "file", field = { "fileGroupType", "fileGroupCode" }, versionField = "version")
	private List<FileDto> fileRefs;

	public FileGroup toModel() {
		FileGroup model = ValueUtils.map(this, FileGroup.class);
		return model;
	}
}
