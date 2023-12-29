package com.emoldino.api.common.resource.composite.notbel.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotBelPostEmailIn {
	private String title;
	private String to;
	private String content;
}
