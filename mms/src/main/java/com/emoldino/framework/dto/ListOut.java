package com.emoldino.framework.dto;

import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ListOut<T> {
	private List<T> content = new ArrayList<>();

	public ListOut(Iterable<T> iterable) {
		iterable.forEach(item -> content.add(item));
	}

	public void add(T item) {
		content.add(item);
	}

	public long getTotalElements() {
		return content == null ? 0 : content.size();
	}
}
