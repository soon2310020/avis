package com.emoldino.api.analysis.resource.composite.prochg.dto;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@SuppressWarnings("serial")
public class ProChgDetailsGetOut extends PageImpl<ProChgDetails> {
	private Long moldId;
	private String moldCode;
	private String dateHourRange;

	public ProChgDetailsGetOut(Page<ProChgDetails> page) {
		super(page.getContent(), page.getPageable(), page.getTotalElements());
	}

	public ProChgDetailsGetOut(List<ProChgDetails> content, Pageable pageable, long total) {
		super(content, pageable, total);
	}

	public ProChgDetailsGetOut(List<ProChgDetails> content) {
		super(content);
	}
}
