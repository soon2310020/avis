package com.emoldino.api.common.resource.base.noti.repository.notirecipientconfig;

import java.time.Instant;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.hibernate.annotations.DynamicUpdate;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.emoldino.api.common.resource.base.noti.enumeration.NotiCode;
import com.emoldino.api.common.resource.base.noti.enumeration.NotiRecipientPriority;
import com.emoldino.api.common.resource.base.noti.enumeration.NotiRecipientType;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(uniqueConstraints = @UniqueConstraint(name = "UX_NOTI_RECIPIENT_CONFIG", columnNames = { "notiCode", "recipientType", "recipientId" }))
@DynamicUpdate
public class NotiRecipientConfig {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Enumerated(EnumType.STRING)
	private NotiCode notiCode;
	@Enumerated(EnumType.STRING)
	private NotiRecipientType recipientType;
	private Long recipientId;

	private Integer position;
	@Enumerated(EnumType.STRING)
	private NotiRecipientPriority recipientPriority;

	@CreatedBy
	@Column(updatable = false)
	private Long createdBy;
	@CreatedDate
	@Column(updatable = false)
	private Instant createdAt;
	@LastModifiedBy
	private Long updatedBy;
	@LastModifiedDate
	private Instant updatedAt;
}
