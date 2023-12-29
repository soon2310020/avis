package com.emoldino.api.common.resource.base.noti.dto;

import java.util.List;

import com.emoldino.api.common.resource.base.noti.enumeration.NotiCategory;
import com.emoldino.api.common.resource.base.noti.enumeration.NotiStatus;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class NotiGetIn {
	private Long id;
	private List<NotiCategory> notiCategory;
	private List<NotiStatus> notiStatus;
}
