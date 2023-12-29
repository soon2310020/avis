package com.emoldino.api.common.resource.base.id.service.idrule;

import com.emoldino.api.common.resource.base.id.enumeration.IdPartType;
import com.emoldino.api.common.resource.base.id.service.IdService.IdPart;
import com.emoldino.api.common.resource.base.id.service.IdService.IdRule;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Arrays;

@Getter
@Setter
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class IdRuleDataRequest extends IdRule {
	public IdRuleDataRequest() {
		super(Arrays.asList(//
				new IdPart(IdPartType.CONSTANT, "DR-"), //
				new IdPart(IdPartType.SEQUENCE, 7)//
		));
	}
}
