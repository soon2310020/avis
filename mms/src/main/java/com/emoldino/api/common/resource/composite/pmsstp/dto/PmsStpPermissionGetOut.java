package com.emoldino.api.common.resource.composite.pmsstp.dto;

import java.util.List;

import com.emoldino.framework.dto.ListOut;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class PmsStpPermissionGetOut extends ListOut<PmsStpPermission> {
	private List<PmsStpResource> contentAvailable;
}
