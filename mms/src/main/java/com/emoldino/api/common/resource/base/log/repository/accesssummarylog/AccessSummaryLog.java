package com.emoldino.api.common.resource.base.log.repository.accesssummarylog;

import java.time.Instant;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;
import lombok.NoArgsConstructor;
import saleson.common.enumeration.CompanyType;

@Entity
@Data
@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
@NoArgsConstructor
public class AccessSummaryLog {
//	public AccessSummaryLog(CompanyType companyType, Long companyId, String apiName, String serverName, String callerInfo, String targetInfo, String paramNames, int callMonth) {
//		this(companyType, companyId, apiName, serverName, callerInfo, targetInfo, paramNames, callMonth, 0, 0, 0, 0, 0);
//	}

//	public AccessSummaryLog(CompanyType companyType, Long companyId, String apiName, String serverName, String callerInfo, String targetInfo, String paramNames, int callMonth,
//			int successCount, int failCount, long minElapsedTimeMillis, long maxElapsedTimeMillis, long totalElapsedTimeMillis) {
//		this(companyType, companyId, apiName, serverName, callerInfo, targetInfo, paramNames, callMonth, successCount, failCount, 0, minElapsedTimeMillis, maxElapsedTimeMillis,
//				totalElapsedTimeMillis);
//	}

	public AccessSummaryLog(CompanyType companyType, Long companyId, String apiName, String serverName, String callerInfo, String targetInfo, String paramNames, int callMonth,
			int successCount, int failCount, long avgElapsedTimeMillis, long minElapsedTimeMillis, long maxElapsedTimeMillis, long totalElapsedTimeMillis) {
		this.companyType = companyType;
		this.companyId = companyId;
		this.apiName = apiName;
		this.serverName = serverName;
		this.callerInfo = callerInfo;
		this.targetInfo = targetInfo;
		this.paramNames = paramNames;
		this.callMonth = callMonth;
		this.successCount = successCount;
		this.failCount = failCount;
		this.avgElapsedTimeMillis = avgElapsedTimeMillis;
		this.minElapsedTimeMillis = minElapsedTimeMillis;
		this.maxElapsedTimeMillis = maxElapsedTimeMillis;
		this.totalElapsedTimeMillis = totalElapsedTimeMillis;
	}

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Enumerated(EnumType.STRING)
	private CompanyType companyType;
	private Long companyId;
	private String apiName;
	private String serverName;
	private String callerInfo;
	private String targetInfo;
	private String paramNames;
	private int callMonth;
	private int callDate;
	private int callHour;

	private int successCount;
	private int failCount;
	private long avgElapsedTimeMillis;
	private long minElapsedTimeMillis;
	private long maxElapsedTimeMillis;
	private long totalElapsedTimeMillis;

	@CreatedDate
	private Instant createdAt;
	@LastModifiedDate
	private Instant updatedAt;
}
