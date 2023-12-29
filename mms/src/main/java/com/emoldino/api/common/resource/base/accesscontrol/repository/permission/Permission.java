package com.emoldino.api.common.resource.base.accesscontrol.repository.permission;

import java.time.Instant;
import java.util.List;

import javax.persistence.AttributeConverter;
import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Converter;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import org.hibernate.annotations.DynamicUpdate;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.emoldino.api.common.resource.base.accesscontrol.repository.rolecontrol.RoleControl;
import com.emoldino.framework.util.ValueUtils;
import com.fasterxml.jackson.core.type.TypeReference;

import lombok.Data;
import lombok.NoArgsConstructor;
import saleson.common.hibernate.converter.BooleanYnConverter;

@Data
@Entity
@EntityListeners(AuditingEntityListener.class)
@DynamicUpdate
public class Permission {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne
	@JoinColumn(name = "role_id")
	private RoleControl role;

	private String resourceType;
	private String resourceId;
	@Convert(converter = BooleanYnConverter.class)
	private boolean enabled = true;

	@Convert(converter = PermissionItemConverter.class)
	private List<PermissionItem> items;

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

	@NoArgsConstructor
	@Converter(autoApply = true)
	public static class PermissionItemConverter implements AttributeConverter<List<PermissionItem>, String> {
		@Override
		public String convertToDatabaseColumn(List<PermissionItem> attribute) {
			return ValueUtils.toJsonStr(attribute);
		}

		@Override
		public List<PermissionItem> convertToEntityAttribute(String dbData) {
			return ValueUtils.fromJsonStr(dbData, new TypeReference<List<PermissionItem>>() {
			});
		}
	}
}
