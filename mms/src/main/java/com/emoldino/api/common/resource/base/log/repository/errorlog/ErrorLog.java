package com.emoldino.api.common.resource.base.log.repository.errorlog;

import java.time.Instant;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;

import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.http.HttpStatus;

import com.emoldino.api.common.resource.base.log.enumeration.ErrorType;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
@NoArgsConstructor
public class ErrorLog {
	public ErrorLog(ErrorType errorType, String errorCode, HttpStatus errorStatus, String errorMessage) {
		this.errorType = errorType;
		this.errorCode = errorCode;
		this.errorStatus = errorStatus;
		this.errorMessage = errorMessage;
	}
	public ErrorLog(ErrorType errorType, String errorCode, HttpStatus errorStatus, String errorMessage, String detailedLog) {
		this.errorType = errorType;
		this.errorCode = errorCode;
		this.errorStatus = errorStatus;
		this.errorMessage = errorMessage;
		this.detailedLog = detailedLog;
	}

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Enumerated(EnumType.STRING)
	private ErrorType errorType;
	private String errorCode;
	@Enumerated(EnumType.STRING)
	private HttpStatus errorStatus;
	private String errorMessage;
	private String errorPointPath;
	private String errorCause;
	@Lob
	private String detailedLog;

	private String serverName;
	private String apiName;
	private String callerInfo;
	private String targetInfo;
	private String headers;
	private String params;
	@Lob
	private String body;

	private Instant startTime;
	private Instant endTime;
	private long elapsedTimeMillis;

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

//	@Column(length = 1)
//	@Convert(converter = BooleanYnConverter.class)
//	private boolean retry;
//	private Long retryGroupId;
//	private int retryCount;o

//	private String logLevel;

//	private String clientType;
//	private String clientName;
//	private String serverName;
//	private String threadName;
}
