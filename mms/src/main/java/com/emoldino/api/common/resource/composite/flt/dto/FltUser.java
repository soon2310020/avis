package com.emoldino.api.common.resource.composite.flt.dto;

import com.emoldino.framework.util.ValueUtils;

import lombok.Data;
import lombok.NoArgsConstructor;
import saleson.model.User;

@Data
@NoArgsConstructor
public class FltUser {
	private Long id;
	private String name;
	private String loginId;

	public FltUser(User user) {
		ValueUtils.map(user, this);
	}

	public String getCode() {
		return loginId;
	}
}
