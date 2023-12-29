package com.emoldino.framework.util;

import org.springframework.data.domain.Page;

public interface Closure1ParamPageNoReturn<T> {
	void execute(Page<T> page);
}
