package com.emoldino.api.common.resource.composite.usrstp.dto;

import java.util.ArrayList;
import java.util.List;

import com.emoldino.api.common.resource.composite.usrstp.dto.UsrStpData.UsrStpPlant;
import com.emoldino.framework.dto.ListOut;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class UsrStpGetPlantsOut extends ListOut<UsrStpPlant> {
	private List<UsrStpRole> availableRoles = new ArrayList<>();
}
