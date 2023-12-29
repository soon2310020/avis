package com.emoldino.framework.repository.jobdata;

import java.time.Instant;

import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import lombok.Data;

@Data
@Entity
@EntityListeners(AuditingEntityListener.class)
public class JobData {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private String executorName;
	private String syncKey;

	@CreatedDate
	private Instant createdAt;

	private String logicName;
	private String logicTxType;

	private String beforeLogicName;
	private String beforeLogicTxType;

	private String afterLogicName;
	private String afterLogicTxType;

	private String throwsLogicName;
	private String throwsLogicTxType;

	@Lob
	private String jobDetail;
}
