package com.emoldino.api.asset.resource.composite.toladt.dto;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@SuppressWarnings("serial")
public class TolAdtRelocationHistoriesGetOut extends PageImpl<TolAdtRelocationHistoriesItem> {
	private Long moldId;
	private String moldCode;

	public TolAdtRelocationHistoriesGetOut(List<TolAdtRelocationHistoriesItem> content) {
		super(content);
	}

	public TolAdtRelocationHistoriesGetOut(List<TolAdtRelocationHistoriesItem> content, Pageable pageable, long total) {
		super(content, pageable, total);
	}

	public TolAdtRelocationHistoriesGetOut(Page<TolAdtRelocationHistoriesItem> page, Long moldId, String moldCode) {
		this(page.getContent(), page.getPageable(), page.getTotalElements());
		this.moldId = moldId;
		this.moldCode = moldCode;
	}

}
