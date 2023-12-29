package com.emoldino.api.common.resource.base.id.service.idrule;

import java.util.Arrays;

import com.emoldino.api.common.resource.base.id.enumeration.IdPartType;
import com.emoldino.api.common.resource.base.id.service.IdService.IdPart;
import com.emoldino.api.common.resource.base.id.service.IdService.IdRule;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class IdRuleWorkOrder extends IdRule {
	public IdRuleWorkOrder() {
		super(Arrays.asList(//
				new IdPart(IdPartType.CONSTANT, "WO-"), //
				new IdPart(IdPartType.SEQUENCE, 7)//
		));
	}
}
