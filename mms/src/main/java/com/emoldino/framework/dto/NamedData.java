package com.emoldino.framework.dto;

import lombok.*;

@Data
public class NamedData<T> {
	private String name;
	private T data;

	public NamedData(String name, T data) {
		this.name = name;
		this.data = data;
	}
}
