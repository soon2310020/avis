package com.emoldino.framework.repository.draftdata;

import java.time.Instant;

import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import org.hibernate.annotations.DynamicUpdate;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import lombok.Data;

@Entity
@DynamicUpdate
@EntityListeners(AuditingEntityListener.class)
@Data
public class DraftData {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private String resourceType;
	private String resourceId;
	private Long userId;

	private String content;

	@CreatedBy
	private Long createdBy;
	@CreatedDate
	private Instant createdAt;
	@LastModifiedBy
	private Long updatedBy;
	@LastModifiedDate
	private Instant updatedAt;
}
