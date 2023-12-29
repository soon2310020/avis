package com.emoldino.api.common.resource.base.code.repository.codedata;

import java.time.Instant;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;
import lombok.NoArgsConstructor;
import saleson.common.hibernate.converter.BooleanYnConverter;
import saleson.model.Company;

@Entity
@Table(indexes = @Index(name = "UX_CODE_DATA", columnList = "companyId,codeType,code", unique = true))
@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
@Data
@NoArgsConstructor
public class CodeData {
	public CodeData(Long companyId, String codeType, String code, String title, String description, String... groupCodes) {
		this.companyId = companyId;
		this.codeType = codeType;
		this.code = code;
		this.title = title;
		this.description = description;
		if (groupCodes != null) {
			int i = 0;
			for (String groupCode : groupCodes) {
				i++;
				if (i == 1) {
					this.group1Code = groupCode;
				} else if (i == 2) {
					this.group2Code = groupCode;
				}
			}
		}
	}

	@Id
	@GeneratedValue
	private Long id;
	private Long companyId;

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "companyId", insertable = false, updatable = false)
	private Company company;

	private String codeType;
	private String code;
	private String title;
	private String description;

	private int position;

	@Column(length = 1)
	@Convert(converter = BooleanYnConverter.class)
	private boolean enabled = true;

	@Column(length = 1)
	@Convert(converter = BooleanYnConverter.class)
	private boolean deleted;

	@CreatedBy
	@JsonIgnore
	private Long createdBy;
	@CreatedDate
	private Instant createdAt;
	@LastModifiedBy
	@JsonIgnore
	private Long updatedBy;
	@LastModifiedDate
	private Instant updatedAt;

	private String group1Code;
	@Transient
	private String group1Title;
	private String group2Code;
	@Transient
	private String group2Title;
}