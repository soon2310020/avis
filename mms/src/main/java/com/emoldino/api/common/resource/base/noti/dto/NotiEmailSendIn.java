package com.emoldino.api.common.resource.base.noti.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotiEmailSendIn {
	private String from;
	private List<String> to;
	private String title;
	private String content;
	private String subtype;
}
