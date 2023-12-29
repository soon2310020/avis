package com.emoldino.framework.dto;

import java.util.List;

import com.emoldino.api.common.resource.base.option.enumeration.MasterFilterMode;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BatchIn {
	private MasterFilterMode selectionMode;
	private List<Long> selectedIds;
	private List<Long> unselectedIds;
}
