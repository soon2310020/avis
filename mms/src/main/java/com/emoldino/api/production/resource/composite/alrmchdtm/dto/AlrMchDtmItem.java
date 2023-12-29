package com.emoldino.api.production.resource.composite.alrmchdtm.dto;

import com.emoldino.api.asset.resource.base.mold.enumeration.ToolingStatus;
import com.emoldino.api.common.resource.base.location.util.LocationUtils;
import com.emoldino.api.common.resource.composite.tolstp.dto.UserLiteDTO;
import com.emoldino.framework.util.DateUtils2;
import com.querydsl.core.annotations.QueryProjection;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import saleson.common.enumeration.*;
import saleson.model.User;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

@Data
@NoArgsConstructor
public class AlrMchDtmItem {
	private Long id;
	private MachineDowntimeAlertStatus alertStatus;
//	private String creationUserName;
	private String creationDateTime;//Alert Date
//	private String confirmUserName;
	private UserLiteDTO confirmBy;

//	private String confirmDateTime;

	private MachineAvailabilityType downtimeType;
	private String startTime;
	private String endTime;
	private Long duration;
	private List<MchDtmReason> downtimeReasonList;

	private Long reportedUserId;
//	private String reportedUserName;
	private UserLiteDTO reportedBy;

	private Long machineId;
	private String machineCode;

	private Long moldId;
	private String moldCode;
	private ToolingStatus toolingStatus;

//	private Long counterId;
//	private String counterCode;
//	private CounterStatus sensorStatus;

//	private Long companyId;
//	private String companyName;
//	private String companyCode;
//	private CompanyType companyType;

	private Long locationId;
	private String locationCode;
	private String locationName;


	@QueryProjection
	public AlrMchDtmItem(Long id, MachineDowntimeAlertStatus alertStatus, Instant createdAt,
						 User confirmUser,
						 MachineAvailabilityType downtimeType,
						 Instant startTime, Instant endTime,// Long duration,
						 Long reportedUserId, User reportedUser,
						 Long machineId, String machineCode,
						 Long moldId, String moldCode, ToolingStatus toolingStatus,
//						 Long companyId, String companyName, String companyCode, CompanyType companyType,
						 Long locationId, String locationCode, String locationName) {
		String zoneId = LocationUtils.getZoneIdByLocationId(locationId);
		this.id = id;
		this.alertStatus = alertStatus;
//		this.creationUserName = creationUserName;
		this.creationDateTime = DateUtils2.format(createdAt, DateUtils2.DatePattern.yyyy_MM_dd_HH_mm_ss, zoneId);
		this.confirmBy = confirmUser != null ? new UserLiteDTO(confirmUser) : null;
//		this.confirmDateTime = DateUtils2.format(confirmedAt, DateUtils2.DatePattern.yyyy_MM_dd_HH_mm_ss, zoneId);
		this.downtimeType = downtimeType;
		this.startTime = DateUtils2.format(startTime, DateUtils2.DatePattern.yyyy_MM_dd_HH_mm_ss, zoneId);
		this.endTime = DateUtils2.format(endTime, DateUtils2.DatePattern.yyyy_MM_dd_HH_mm_ss, zoneId);
		this.duration = Duration.between(startTime, (endTime == null || endTime.isAfter(Instant.now())) ? Instant.now() : endTime).getSeconds();

		this.reportedUserId = reportedUserId;
		this.reportedBy = reportedUser!=null? new UserLiteDTO(reportedUser) : null;

		this.machineId = machineId;
		this.machineCode = machineCode;

		this.moldId = moldId;
		this.moldCode = moldCode;
		this.toolingStatus = toolingStatus;

//		this.companyId = companyId;
//		this.companyName = companyName;
//		this.companyCode = companyCode;
//		this.companyType = companyType;

		this.locationId = locationId;
		this.locationCode = locationCode;
		this.locationName = locationName;
	}

	@Data
	@NoArgsConstructor
	public static class MchDtmReason {
		private Long machineDowntimeAlertId;
		private String reason;
		private String note;

		public MchDtmReason(Long machineDowntimeAlertId, String reason, String note) {
			this.machineDowntimeAlertId = machineDowntimeAlertId;
			this.reason = reason;
			this.note = note;
		}

		public MchDtmReason(String reason, String note) {
			this.reason = reason;
			this.note = note;
		}
	}
}
