package com.emoldino.api.analysis.resource.base.command.repository.devicecommand;

import java.time.Instant;

import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;

import org.hibernate.annotations.DynamicUpdate;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@DynamicUpdate
@EntityListeners(AuditingEntityListener.class)
@Data
@NoArgsConstructor
public class DeviceCommand {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private String deviceId;
	private String deviceType;
	private String command;
	private Integer indexNo = 0;
	private String status;
	@Lob
	private String data;
	private String comment;

	@CreatedBy
	private Long createdBy;
	@CreatedDate
	private Instant createdAt;
	@LastModifiedBy
	private Long updatedBy;
	@LastModifiedDate
	private Instant updatedAt;
}
