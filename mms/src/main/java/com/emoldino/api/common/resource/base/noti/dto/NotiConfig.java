package com.emoldino.api.common.resource.base.noti.dto;

import java.util.List;

import com.emoldino.api.common.resource.base.noti.dto.NotiPostIn.NotiPostRecipient;
import com.emoldino.api.common.resource.base.noti.enumeration.NotiLinkType;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.Data;
import saleson.common.enumeration.PriorityType;

@Data
@JsonInclude(Include.NON_NULL)
public class NotiConfig {
	private PriorityType notiPriority;
	private String description;

	private boolean webEnabled;
	private boolean mobileEnabled;
	private boolean emailEnabled;

	private boolean taskStatusEnabled;
	private boolean procStatusEnabled;

	private NotiLinkType linkType;
	private String linkTo;

	private List<NotiPostRecipient> recipients;

	public NotiLinkType getLinkType() {
		return linkType == null ? NotiLinkType.PAGE_POPUP : linkType;
	}
}
