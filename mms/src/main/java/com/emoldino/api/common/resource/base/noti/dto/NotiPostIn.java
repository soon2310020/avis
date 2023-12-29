package com.emoldino.api.common.resource.base.noti.dto;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.util.ObjectUtils;

import com.emoldino.api.common.resource.base.accesscontrol.repository.rolecontrol.RoleControl;
import com.emoldino.api.common.resource.base.accesscontrol.repository.rolecontrol.RoleControlRepository;
import com.emoldino.api.common.resource.base.noti.enumeration.NotiLinkType;
import com.emoldino.api.common.resource.base.noti.enumeration.NotiRecipientPriority;
import com.emoldino.api.common.resource.base.noti.enumeration.NotiRecipientType;
import com.emoldino.framework.util.BeanUtils;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import saleson.api.user.UserRepository;
import saleson.common.enumeration.PriorityType;
import saleson.model.User;

@Data
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(Include.NON_NULL)
public class NotiPostIn {
//	private Long relatedCompanyId;
//	private Long companyId;
//	private Long locationId;

	private Boolean webEnabled;
	private Boolean mobileEnabled;
	private Boolean emailEnabled;

	private PriorityType notiPriority;
	private Long senderId;
	private String senderName;
	private String senderEmail;

	private NotiLinkType linkType;
	private String linkTo;

	private List<NotiPostRecipient> recipients;

	private boolean contentByUser;
	private String title;
	private String content;
	private String emailTitle;
	private String emailSubtype;
	private String emailContent;

	private Long dataId;

	Map<String, Object> params;

	@JsonIgnore
	public NotiPostIn setSender(User sender) {
		senderId = sender == null ? null : sender.getId();
		senderName = sender == null ? null : sender.getName();
		senderEmail = sender == null ? null : sender.getEmail();
		return this;
	}

	@JsonIgnore
	public NotiPostIn setSender(Long id) {
		User sender = BeanUtils.get(UserRepository.class).findById(id).orElse(null);
		setSender(sender);
		return this;
	}

	@JsonIgnore
	public NotiPostIn setSenderByEmail(String email) {
		User sender = BeanUtils.get(UserRepository.class).findByEmailAndDeletedIsFalse(email).orElse(null);
		setSender(sender);
		return this;
	}

	public NotiPostIn addRecipients(List<Long> userIds) {
		if (!ObjectUtils.isEmpty(userIds)) {
			userIds.forEach(userId -> addRecipient(userId));
		}
		return this;
	}

	public NotiPostIn addRecipient(Long userId) {
		addRecipient(NotiPostRecipient.builder().recipientType(NotiRecipientType.USER).recipientId(userId).build());
		return this;
	}

	public NotiPostIn addRecipient(NotiPostRecipient recipient) {
		if (this.recipients == null) {
			this.recipients = new ArrayList<>();
		}
		this.recipients.add(recipient);
		return this;
	}

	public Map<String, Object> getParams() {
		if (this.params == null) {
			this.params = new LinkedHashMap<>();
		}
		return this.params;
	}

	public NotiPostIn setParam(String name, Object value) {
		getParams().put(name, value);
		return this;
	}

	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	@Builder
	public static class NotiPostRecipient {
		private NotiRecipientType recipientType;
		private Long recipientId;
		@Getter(AccessLevel.PRIVATE)
		private String value;
		private NotiRecipientPriority recipientPriority;

		public void setType(NotiRecipientType type) {
			this.recipientType = type;
			if (type != null && value != null) {
				setValue(value);
			}
		}

		public void setValue(String value) {
			this.value = value;
			if (this.recipientType != null && !ObjectUtils.isEmpty(value)) {
				if (NotiRecipientType.USER.equals(this.recipientType)) {
					User user = BeanUtils.get(UserRepository.class).findByEmailAndDeletedIsFalse(value).orElse(null);
					this.recipientId = user == null ? null : user.getId();
				} else if (NotiRecipientType.ROLE.equals(this.recipientType)) {
					RoleControl role = BeanUtils.get(RoleControlRepository.class).findByAuthority(value);
					this.recipientId = role == null ? null : role.getId();
				}
			}
		}
	}
}
