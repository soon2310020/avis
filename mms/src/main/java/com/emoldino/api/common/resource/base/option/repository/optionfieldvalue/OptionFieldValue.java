package com.emoldino.api.common.resource.base.option.repository.optionfieldvalue;

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
import javax.persistence.Index;
import javax.persistence.Table;

import org.hibernate.annotations.DynamicUpdate;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import lombok.Data;
import lombok.NoArgsConstructor;
import saleson.common.enumeration.ConfigCategory;
import saleson.common.hibernate.converter.BooleanYnConverter;

@Data
@NoArgsConstructor
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(indexes = @Index(name = "UX_OPTION_FIELD_VALUE", columnList = "configCategory,userId,fieldName", unique = true))
@DynamicUpdate
public class OptionFieldValue {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	@Enumerated(EnumType.STRING)
	private ConfigCategory configCategory;
	private String optionName;
	private Long userId;
	private String fieldName;
	private int position;
	private String dataType;
	private String value;

	@Convert(converter = BooleanYnConverter.class)
	private boolean deleted;

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

	public void setConfigCategory(ConfigCategory configCategory) {
		this.configCategory = configCategory;
		this.optionName = configCategory.name();
	}

	public String getOptionName() {
		return optionName == null && configCategory != null ? configCategory.name() : optionName;
	}
}
