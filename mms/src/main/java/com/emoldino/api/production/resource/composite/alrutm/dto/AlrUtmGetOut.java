package com.emoldino.api.production.resource.composite.alrutm.dto;

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
public class AlrUtmGetOut extends PageImpl<AlrUtmItem> {
	private List<Tab> tabs;

	public AlrUtmGetOut(Page<AlrUtmItem> page) {
		this(page.getContent(), page.getPageable(), page.getTotalElements());
	}

	public AlrUtmGetOut(Page<AlrUtmItem> page, List<Tab> tabs) {
		this(page.getContent(), page.getPageable(), page.getTotalElements());
		setTabs(tabs);
	}

	public AlrUtmGetOut(List<AlrUtmItem> content, Pageable pageable, long total) {
		super(content, pageable, total);
	}
}
