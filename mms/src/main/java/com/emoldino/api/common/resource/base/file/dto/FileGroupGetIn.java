package com.emoldino.api.common.resource.base.file.dto;

import java.util.List;

import com.emoldino.api.common.resource.base.file.enumeration.FileGroupStatus;
import com.emoldino.api.common.resource.base.file.enumeration.FileGroupType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FileGroupGetIn {
	private String query;
	private FileGroupType fileGroupType;
	private String fileGroupCode;
	private List<FileGroupStatus> fileGroupStatus;

	private boolean managedTypeOnly;
	private boolean groupByCode;
	private Boolean enabled;
}
