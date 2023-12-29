package com.emoldino.api.integration.resource.composite.pqwrk.dto;

import lombok.Data;

@Data
public class AiPqStdInfo {
	private Long counterId;
	private Long partId;
	private Long companyId;
	private Long categoryId;
	private Long parentId;

	public AiPqStdInfo(Long counterId, Long partId, Long companyId, Long categoryId, Long parentId) {
		this.counterId = counterId;
		this.partId = partId;
		this.companyId = companyId;
		this.categoryId = categoryId;
		this.parentId = parentId;
	}
}
