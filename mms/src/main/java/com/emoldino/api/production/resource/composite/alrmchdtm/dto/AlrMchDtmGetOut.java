package com.emoldino.api.production.resource.composite.alrmchdtm.dto;

import com.emoldino.framework.dto.Tab;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;

@Getter
@Setter
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@SuppressWarnings("serial")
public class AlrMchDtmGetOut extends PageImpl<AlrMchDtmItem> {
	private List<Tab> tabs;

	public AlrMchDtmGetOut(Page<AlrMchDtmItem> page) {
		this(page.getContent(), page.getPageable(), page.getTotalElements());
	}

	public AlrMchDtmGetOut(Page<AlrMchDtmItem> page, List<Tab> tabs) {
		this(page.getContent(), page.getPageable(), page.getTotalElements());
		setTabs(tabs);
	}

	public AlrMchDtmGetOut(List<AlrMchDtmItem> content, Pageable pageable, long total) {
		super(content, pageable, total);
	}

}
