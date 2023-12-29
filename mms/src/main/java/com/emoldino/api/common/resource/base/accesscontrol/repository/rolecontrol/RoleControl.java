package com.emoldino.api.common.resource.base.accesscontrol.repository.rolecontrol;

import java.time.Instant;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.Table;

import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Setter;
import saleson.common.enumeration.RoleType;
import saleson.common.hibernate.converter.BooleanYnConverter;

@Data
@NoArgsConstructor
@Entity
@Table(name = "ROLE")
@EntityListeners(AuditingEntityListener.class)
public class RoleControl {
	@Id
	private Long id;

	@Column(unique = true)
	private String authority;
	private String name;
	private String description;

	@Setter(AccessLevel.PRIVATE)
	@Enumerated(EnumType.STRING)
	@JsonIgnore
	private RoleType roleType = RoleType.ROLE_CONTROL;

	@Convert(converter = BooleanYnConverter.class)
	private boolean enabled;
	@Convert(converter = BooleanYnConverter.class)
	private Boolean emoldinoEnabled = false;
	@Convert(converter = BooleanYnConverter.class)
	private Boolean oemEnabled = false;
	@Convert(converter = BooleanYnConverter.class)
	private Boolean supplierEnabled = false;
	@Convert(converter = BooleanYnConverter.class)
	private Boolean toolmakerEnabled = false;

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
