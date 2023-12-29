package com.emoldino.api.common.resource.base.noti.repository.notirecipient;

import java.time.Instant;

import javax.persistence.Column;
import javax.persistence.Convert;
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
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.emoldino.api.common.resource.base.noti.enumeration.NotiStatus;

import lombok.Data;
import lombok.NoArgsConstructor;
import saleson.common.enumeration.Language;
import saleson.common.hibernate.converter.BooleanYnConverter;

@Data
@NoArgsConstructor
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(uniqueConstraints = @UniqueConstraint(name = "UX_NOTI_RECIPIENT", columnNames = { "notiId", "userId" }))
@DynamicUpdate
public class NotiRecipient {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private Long notiId;
	private Long userId;

	@Enumerated(EnumType.STRING)
	private Language language;
	@Convert(converter = BooleanYnConverter.class)
	private boolean contentByUser;

	private Integer position;

	@Enumerated(EnumType.STRING)
	private NotiStatus notiStatus;
	private Instant readAt;
	@Convert(converter = BooleanYnConverter.class)
	private boolean deleted;
	private Instant deletedAt;

	@CreatedBy
	@Column(updatable = false)
	private Long createdBy;
	@CreatedDate
	@Column(updatable = false)
	private Instant createdAt;
}
