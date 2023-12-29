package com.emoldino.framework.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Tab {
	private String tabName;
	private long totalElements;
	private boolean defaultTab;
}
