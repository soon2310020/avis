package com.emoldino.api.common.resource.base.noti.repository.noti;

import java.time.Instant;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import org.hibernate.annotations.DynamicUpdate;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.emoldino.api.common.resource.base.noti.enumeration.NotiCategory;
import com.emoldino.api.common.resource.base.noti.enumeration.NotiCode;
import com.emoldino.api.common.resource.base.noti.enumeration.NotiProcStatus;

import lombok.Data;
import lombok.NoArgsConstructor;
import saleson.common.enumeration.PriorityType;

@Data
@NoArgsConstructor
@Entity
@EntityListeners(AuditingEntityListener.class)
@DynamicUpdate
public class Noti {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Enumerated(EnumType.STRING)
	private NotiCode notiCode;
	@Enumerated(EnumType.STRING)
	private NotiCategory notiCategory;
	private Long dataId;

	@Enumerated(EnumType.STRING)
	private PriorityType notiPriority;
	@Enumerated(EnumType.STRING)
	private NotiProcStatus taskStatus;
	@Enumerated(EnumType.STRING)
	private NotiProcStatus procStatus;

	@Column(updatable = false)
	private Long senderId;
	@Column(updatable = false)
	private Instant sentAt;

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
