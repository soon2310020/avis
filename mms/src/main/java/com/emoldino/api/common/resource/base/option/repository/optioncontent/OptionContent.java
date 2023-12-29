package com.emoldino.api.common.resource.base.option.repository.optioncontent;

import java.time.Instant;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Lob;
import javax.persistence.Table;

import org.hibernate.annotations.DynamicUpdate;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(indexes = @Index(name = "UX_OPTION_CONTENT", columnList = "optionName,userId", unique = true))
@DynamicUpdate
public class OptionContent {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private String optionName;
	private Long userId;
	@Lob
	private String content;

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
