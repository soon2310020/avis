package com.emoldino.api.common.resource.base.file.dto;

import com.emoldino.api.common.resource.base.file.repository.fileitem.FileItem;
import com.emoldino.framework.util.ValueUtils;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FileDto {
	@ApiModelProperty(value = "File ID (Long) or Temporal File ID (UUID)", required = true)
	private String id;
	@ApiModelProperty(value = "File No.")
	private String fileNo;
	@ApiModelProperty(value = "File Name")
	private String fileName;
	@ApiModelProperty(value = "Content Type - application/octet-stream")
	private String contentType;
	@ApiModelProperty(value = "File Size")
	private long fileSize;
	@ApiModelProperty(value = "File Path for Access - /api/common/fle-tmp/{fileGroupKey}~{id}")
	private String filePath;

	public static FileDto fromModel(FileItem model) {
		FileDto fileDto = ValueUtils.map(model, FileDto.class);
		fileDto.setFilePath("/api/common/fle-tmp/" + model.getFileGroupKey() + "~" + model.getId());
		return fileDto;
	}
}
