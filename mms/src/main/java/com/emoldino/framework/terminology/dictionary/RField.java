package com.emoldino.framework.terminology.dictionary;

import java.time.Instant;

import javax.persistence.Column;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;

import com.emoldino.api.asset.resource.base.maintenance.enumeration.RECURR_CONSTRAINT_TYPE;
import com.emoldino.api.common.resource.base.noti.enumeration.NotiRecipientPriority;
import com.emoldino.api.common.resource.base.noti.enumeration.NotiRecipientType;

public class RField {

	Instant readAt;

	Long recentShotCount;

	Long recipientId;

	@Enumerated(EnumType.STRING)
	@Column(length = 20)
	NotiRecipientPriority recipientPriority;

	@Enumerated(EnumType.STRING)
	@Column(length = 20)
	NotiRecipientType recipientType;

	@Enumerated(EnumType.STRING)
	@Column(length = 20)
	RECURR_CONSTRAINT_TYPE recurrConstraintType;

	@Column(length = 8)
	String recurrDueDate;

	Integer recurrNum;

	@Column(length = 100)
	String refCode;
	Long refId;

	Double requiredQty;

}
