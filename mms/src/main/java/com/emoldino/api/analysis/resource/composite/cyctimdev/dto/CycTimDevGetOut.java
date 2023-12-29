package com.emoldino.api.analysis.resource.composite.cyctimdev.dto;

import java.util.List;

import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@SuppressWarnings("serial")
@Getter
@Setter
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class CycTimDevGetOut extends PageImpl<CycTimDevItem> {

	public CycTimDevGetOut(List<CycTimDevItem> content, Pageable pageable, long total) {
		super(content, pageable, total);
	}

}
