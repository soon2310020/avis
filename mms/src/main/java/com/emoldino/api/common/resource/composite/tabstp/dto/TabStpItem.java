package com.emoldino.api.common.resource.composite.tabstp.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TabStpItem {
	private Long id;
	private String name;
	private boolean defaultTab;
	private boolean shown;
}
