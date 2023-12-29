package com.emoldino.api.common.resource.composite.notbel.dto;

import java.util.List;

import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@SuppressWarnings("serial")
public class NotBelGetOut extends PageImpl<NotBelItem> {
	private boolean on;

	public NotBelGetOut(List<NotBelItem> content) {
		super(content);
	}

	public NotBelGetOut(List<NotBelItem> content, Pageable pageable, long total) {
		super(content, pageable, total);
	}

	public NotBelGetOut(List<NotBelItem> content, Pageable pageable, long total, boolean on) {
		super(content, pageable, total);
		this.on = on;
	}

}
