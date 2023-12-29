package com.emoldino.api.common.resource.base.file.repository.filegroup;

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

import org.hibernate.annotations.DynamicUpdate;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.emoldino.api.common.resource.base.file.enumeration.FileGroupStatus;
import com.emoldino.api.common.resource.base.file.enumeration.FileGroupType;
import com.emoldino.framework.util.ValueUtils;

import lombok.Data;
import lombok.NoArgsConstructor;
import saleson.common.hibernate.converter.BooleanYnConverter;

@Data
@NoArgsConstructor
@Entity
@EntityListeners(AuditingEntityListener.class)
@DynamicUpdate
public class FileGroup {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private String fileGroupKey;
	private String paramName;
	private String version;
	private Long versionNo;

	@Enumerated(EnumType.STRING)
	private FileGroupStatus fileGroupStatus;

	@Enumerated(EnumType.STRING)
	private FileGroupType fileGroupType;
	private String fileGroupCode;
	private String fileGroupName;
	private String description;
	private Instant releasedAt;

	@Convert(converter = BooleanYnConverter.class)
	private boolean enabled = true;

	private String prefix;
	private String fileGroupKey1;
	private String fileGroupKey2;
	private String fileGroupKey3;
	private String fileGroupKey4;
	private String fileGroupKey5;

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

	public FileGroup(FileGroupType fileGroupType, String fileGroupCode) {
		this.fileGroupType = fileGroupType;
		this.fileGroupCode = fileGroupCode;
	}

	public void setVersion(String version) {
		versionNo = ValueUtils.toVersionNo(version);
		this.version = version;
	}

}
