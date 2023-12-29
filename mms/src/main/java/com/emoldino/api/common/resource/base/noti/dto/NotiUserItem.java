package com.emoldino.api.common.resource.base.noti.dto;

import java.time.Instant;

import org.springframework.util.ObjectUtils;

import com.emoldino.api.common.resource.base.noti.enumeration.NotiCategory;
import com.emoldino.api.common.resource.base.noti.enumeration.NotiCode;
import com.emoldino.api.common.resource.base.noti.enumeration.NotiLinkType;
import com.emoldino.api.common.resource.base.noti.enumeration.NotiStatus;
import com.emoldino.framework.util.DateUtils2;
import com.emoldino.framework.util.DateUtils2.DatePattern;
import com.emoldino.framework.util.HttpUtils;
import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import saleson.common.enumeration.PriorityType;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotiUserItem {
	private Long id;

	private NotiCode notiCode;
	private NotiCategory notiCategory;
	private PriorityType notiPriority;
	private Long senderId;
	private String senderName;
	@JsonIgnore
	private Instant sentAt;

	@JsonIgnore
	private Long notiRecipientId;
	private NotiStatus notiStatus;
	@JsonIgnore
	private Instant readAt;

	private String content;

	private NotiLinkType linkType;
	private String linkTo;

	public NotiUserItem(//
			Long id, //
			NotiCode notiCode, //
			NotiCategory notiCategory, //
			PriorityType notiPriority, //
			Long senderId, //
			String senderName, //
			Instant sentAt, //
			Long notiRecipientId, //
			NotiStatus notiStatus, //
			Instant readAt //
	) {
		this(id, notiCode, notiCategory, notiPriority, senderId, senderName, sentAt, notiRecipientId, notiStatus, readAt, null, null, null);
	}

	public NotiUserItem(//
			Long id, //
			NotiCode notiCode, //
			NotiCategory notiCategory, //
			PriorityType notiPriority, //
			Long senderId, //
			String senderName, //
			Instant sentAt, //
			Long notiRecipientId, //
			NotiStatus notiStatus, //
			Instant readAt, //
			String content, //
			NotiLinkType linkType, //
			String linkTo, //
			String content2, //
			NotiLinkType linkType2, //
			String linkTo2//
	) {
		this(id, notiCode, notiCategory, notiPriority, senderId, senderName, sentAt, notiRecipientId, notiStatus, readAt, content, linkType, linkTo);
		if (ObjectUtils.isEmpty(content) && !ObjectUtils.isEmpty(content2)) {
			this.content = content2;
			this.linkType = linkType2;
			this.linkTo = linkTo2;
		}
	}

	public String getSentDateTime() {
		return DateUtils2.format(sentAt, DatePattern.yyyy_MM_dd_HH_mm_ss, HttpUtils.getTimeZone());
	}

	public String getReadDateTime() {
		return DateUtils2.format(readAt, DatePattern.yyyy_MM_dd_HH_mm_ss, HttpUtils.getTimeZone());
	}
}
