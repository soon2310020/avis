package com.emoldino.api.common.resource.composite.pltstp.dto;

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
public class PltStpGetOut extends PageImpl<PltStpItem> {
	private List<Tab> tabs;

	public PltStpGetOut(Page<PltStpItem> page) {
		this(page.getContent(), page.getPageable(), page.getTotalElements());
	}

	public PltStpGetOut(Page<PltStpItem> page, List<Tab> tabs) {
		this(page.getContent(), page.getPageable(), page.getTotalElements());
		setTabs(tabs);
	}

	public PltStpGetOut(List<PltStpItem> content, Pageable pageable, long total) {
		super(content, pageable, total);
	}
}
