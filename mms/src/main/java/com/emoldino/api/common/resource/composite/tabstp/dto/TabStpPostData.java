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
public class TabStpPostData {
	private String name;
	private List<Long> selectedIds;
}
