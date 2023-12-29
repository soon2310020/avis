package com.emoldino.api.common.resource.composite.alrdet.dto;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import com.emoldino.framework.dto.Tab;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@SuppressWarnings("serial")
public class AlrDetGetOut extends PageImpl<AlrDetItem> {
	private List<Tab> tabs;

	public AlrDetGetOut(Page<AlrDetItem> page) {
		this(page.getContent(), page.getPageable(), page.getTotalElements());
	}

	public AlrDetGetOut(Page<AlrDetItem> page, List<Tab> tabs) {
		this(page.getContent(), page.getPageable(), page.getTotalElements());
		setTabs(tabs);
	}

	public AlrDetGetOut(List<AlrDetItem> content, Pageable pageable, long total) {
		super(content, pageable, total);
	}
}
