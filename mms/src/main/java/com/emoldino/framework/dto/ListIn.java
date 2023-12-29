package com.emoldino.framework.dto;

import java.util.*;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ListIn<T> {
	private List<T> content = new ArrayList<>();

	public void add(T item) {
		content.add(item);
	}
}
