package saleson.model.customField;

import java.time.Instant;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Lob;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Data;
import saleson.common.enumeration.ObjectType;
import saleson.common.enumeration.customField.CustomFieldType;
import saleson.common.enumeration.customField.PropertyGroup;
import saleson.common.hibernate.converter.BooleanYnConverter;

@Data
@Entity
@EntityListeners(AuditingEntityListener.class)
public class CustomField {
	@Id
	@GeneratedValue
	private Long id;

	@Enumerated(EnumType.STRING)
	private ObjectType objectType;
	@Enumerated(EnumType.STRING)
	private PropertyGroup propertyGroup;

	private String fieldName;

	@Enumerated(EnumType.STRING)
	CustomFieldType fieldType = CustomFieldType.TEXT;
	private Integer position = 0;

	@Convert(converter = BooleanYnConverter.class)
	private boolean defaultField;
	@Convert(converter = BooleanYnConverter.class)
	private boolean hidden;
	@Convert(converter = BooleanYnConverter.class)
	private boolean deleted;

	@Convert(converter = BooleanYnConverter.class)
	private Boolean required;
	@Convert(converter = BooleanYnConverter.class)
	private Boolean defaultInput;
	@Lob
	private String defaultInputValue;

	@CreatedDate
	@Column(updatable = false)
	@JsonIgnore
	private Instant createdAt;
	@LastModifiedDate
	@JsonIgnore
	private Instant updatedAt;
}
