package com.emoldino.api.analysis.resource.base.production.dto;

import java.util.Map;

import org.springframework.util.StringUtils;

import com.emoldino.framework.util.Closure;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ProdTooltip {
	private boolean enabled;
	private String value;
	private Map<String, Object> data;

	public ProdTooltip(boolean enabled) {
		this.enabled = enabled;
	}

	public ProdTooltip(String value) {
		setValue(value);
	}

	public ProdTooltip(String value, Closure<Map<String, Object>> map) {
		setValue(value);
		this.data = map == null ? null : map.execute();
	}

	public void setValue(String value) {
		this.enabled = !StringUtils.isEmpty(value);
		this.value = value;
	}
}
