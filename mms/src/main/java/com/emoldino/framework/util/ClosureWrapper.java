package com.emoldino.framework.util;

import lombok.*;

@Data
@AllArgsConstructor
public class ClosureWrapper<T> {
	private String name;
	private Closure<T> closure;
}
