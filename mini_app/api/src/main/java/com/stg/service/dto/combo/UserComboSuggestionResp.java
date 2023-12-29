package com.stg.service.dto.combo;

import java.util.Map;

import com.stg.constant.ComboCode;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserComboSuggestionResp {

	private ComboCode code;
	private Map<String, Object> attributes;
}
