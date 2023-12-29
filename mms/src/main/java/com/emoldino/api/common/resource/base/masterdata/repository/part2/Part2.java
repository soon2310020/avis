package com.emoldino.api.common.resource.base.masterdata.repository.part2;

import java.time.Instant;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

import org.hibernate.annotations.DynamicUpdate;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import lombok.Data;
import lombok.NoArgsConstructor;
import saleson.common.hibernate.converter.BooleanYnConverter;

@Data
@NoArgsConstructor
@Entity(name = "PART")
@EntityListeners(AuditingEntityListener.class)
@DynamicUpdate
public class Part2 {
	@Id
	@GeneratedValue
	private Long id;

	private String name;
	private String partCode;

	@Convert(converter = BooleanYnConverter.class)
	private boolean enabled;

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
