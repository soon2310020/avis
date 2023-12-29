package com.emoldino.api.asset.resource.composite.alreol.dto;

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
public class AlrEolGetOut extends PageImpl<AlrEolItem> {
	private List<Tab> tabs;

	public AlrEolGetOut(Page<AlrEolItem> page) {
		this(page.getContent(), page.getPageable(), page.getTotalElements());
	}

	public AlrEolGetOut(Page<AlrEolItem> page, List<Tab> tabs) {
		this(page.getContent(), page.getPageable(), page.getTotalElements());
		setTabs(tabs);
	}

	public AlrEolGetOut(List<AlrEolItem> content, Pageable pageable, long total) {
		super(content, pageable, total);
	}
}
