package com.emoldino.framework.dto;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;

@SuppressWarnings("serial")
public class RestPage<T> extends PageImpl<T> {
	@JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
	public RestPage(//
			@JsonProperty("content") List<T> content, //
			@JsonProperty("number") int number, //
			@JsonProperty("size") int size, //
			@JsonProperty("totalElements") Long totalElements, //
			@JsonProperty("pageable") JsonNode pageable, //
			@JsonProperty("last") boolean last, //
			@JsonProperty("totalPages") int totalPages, //
			@JsonProperty("sort") JsonNode sort, //
			@JsonProperty("first") boolean first, //
			@JsonProperty("numberOfElements") int numberOfElements//
	) {
		super(content, PageRequest.of(number, size), totalElements);
	}

	public RestPage(Page<T> page) {
		super(page.getContent(), page.getPageable(), page.getTotalElements());
	}

	public RestPage(List<T> content, Pageable pageable, long total) {
		super(content, pageable, total);
	}

	public RestPage(List<T> content) {
		super(content);
	}

	public RestPage() {
		super(new ArrayList<T>());
	}
}
