package com.emoldino.api.common.resource.composite.flt.dto;

import java.util.List;

import com.emoldino.api.common.resource.base.option.enumeration.MasterFilterMode;
import com.emoldino.api.common.resource.base.option.enumeration.MasterFilterResourceType;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(Include.NON_NULL)
public class FltResource {
	private MasterFilterResourceType resourceType;
	private MasterFilterMode mode;
	private Long selectedCount;
	private List<Long> selectedIds;
	private List<Long> unselectedIds;
}
