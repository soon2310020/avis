package com.emoldino.api.analysis.resource.base.data.repository.data3collected;

import java.time.Instant;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;

import org.hibernate.annotations.DynamicUpdate;

import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@DynamicUpdate
@Data
@NoArgsConstructor
public class Data3Collected {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	private String requestId;
	private Integer position;
	private Long dataId;

	// CREATED -> REFINE_READY -> DISTRIBUTED -> REFINING -> REFINED
	private String procStatus;
	private Integer procStep;
	private Long procErrorId;

	private Instant occurredAt;
	private Instant sentAt;
	private Instant createdAt;
	private Instant distributedAt;
	private Instant analyzedAt;
	private Instant updatedAt;

	private String dataType;
	@Lob
	private String data;
	private Integer sn;

	private String deviceId;
	private String deviceType;
	private String deviceSwVersion;

	private String brokerId;
	private String brokerType;
	private String brokerSwVersion;
}
