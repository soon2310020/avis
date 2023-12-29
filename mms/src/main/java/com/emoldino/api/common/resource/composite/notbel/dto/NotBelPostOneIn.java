package com.emoldino.api.common.resource.composite.notbel.dto;

import java.util.Map;

import com.emoldino.api.common.resource.base.noti.enumeration.NotiCode;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import saleson.common.enumeration.PriorityType;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotBelPostOneIn {
	private NotiCode notiCode;
	private PriorityType notiPriority;
	private Long senderId;
	private String content;
	private Long dataId;
	private Map<String, Object> params;
}
