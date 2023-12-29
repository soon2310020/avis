package com.emoldino.api.common.resource.composite.tabstp.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TabStpData {
	private Long id;
	private String name;
	private boolean defaultTab;
	private boolean shown;
	private List<Long> selectedIds;
}
