package com.emoldino.api.common.resource.base.noti.repository.noticontent;

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
import javax.persistence.Lob;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.hibernate.annotations.DynamicUpdate;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.emoldino.api.common.resource.base.noti.enumeration.NotiLinkType;

import lombok.Data;
import lombok.NoArgsConstructor;
import saleson.common.enumeration.Language;
import saleson.common.hibernate.converter.BooleanYnConverter;

@Data
@NoArgsConstructor
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(uniqueConstraints = @UniqueConstraint(name = "UX_NOTI_CONTENT", columnNames = { "notiId", "userId", "language" }))
@DynamicUpdate
public class NotiContent {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private Long notiId;
	private Long userId;
	@Enumerated(EnumType.STRING)
	private Language language;

	@Convert(converter = BooleanYnConverter.class)
	private boolean webEnabled;
	@Convert(converter = BooleanYnConverter.class)
	private boolean mobileEnabled;
	@Convert(converter = BooleanYnConverter.class)
	private boolean emailEnabled;

	@Enumerated(EnumType.STRING)
	private NotiLinkType linkType;
	private String linkTo;

	private String title;
	@Lob
	private String content;
	private String emailTitle;
	private String emailSubtype;
	@Lob
	private String emailContent;

	@CreatedBy
	@Column(updatable = false)
	private Long createdBy;
	@CreatedDate
	@Column(updatable = false)
	private Instant createdAt;
}
