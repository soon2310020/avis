package com.emoldino.api.common.resource.base.id.dto;

import java.util.LinkedHashMap;

import com.emoldino.api.common.resource.base.id.enumeration.IdRuleCode;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@SuppressWarnings("serial")
public class IdGenIn extends LinkedHashMap<String, Object> {
	private IdRuleCode idRuleCode;
	private boolean test;
}
