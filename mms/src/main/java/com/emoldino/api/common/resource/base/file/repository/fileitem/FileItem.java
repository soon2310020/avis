package com.emoldino.api.common.resource.base.file.repository.fileitem;

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

import com.emoldino.api.common.resource.base.file.enumeration.FileStatus;
import com.emoldino.api.common.resource.base.file.enumeration.StorageType;

import lombok.Data;

@Data
@Entity
@EntityListeners(AuditingEntityListener.class)
@DynamicUpdate
public class FileItem {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private String fileGroupKey;
	private String paramName;
	private String version;
	private int position;
	private String fileName;

	// 1. Upload Temporal File
	// 2. CREATED -> MOVING(File) -> MOVED(File-{company}/{year}/{month}{day}/{uuid}) -> SAVED(Data)
	// 3. REMOVING(File) -> REMOVED(File) -> DELETED(Data)
	@Enumerated(EnumType.STRING)
	private FileStatus fileStatus;

	// Data Import Template - Tooling, Sensor's FW, Work Instruction, Blueprint
	private String fileType;
	private String fileNo;
	private String description;

	private String contentType;
	private long fileSize;
	@Enumerated(EnumType.STRING)
	private StorageType storageType;
	private String fileLocation;

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
