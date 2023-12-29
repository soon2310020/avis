package com.emoldino.api.common.resource.composite.notbel.dto;

import com.emoldino.api.common.resource.base.noti.enumeration.NotiCategory;
import com.emoldino.api.common.resource.base.noti.enumeration.NotiStatus;

import lombok.Data;

@Data
public class NotBelGetIn {
	private NotiCategory notiCategory;
	private NotiStatus notiStatus;
}
