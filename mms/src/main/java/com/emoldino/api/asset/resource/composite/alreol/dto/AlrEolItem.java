package com.emoldino.api.asset.resource.composite.alreol.dto;

import java.time.Instant;

import com.emoldino.api.asset.resource.base.mold.enumeration.ToolingStatus;
import com.emoldino.api.common.resource.base.location.util.LocationUtils;
import com.emoldino.framework.util.DateUtils2;
import com.querydsl.core.annotations.QueryProjection;

import lombok.Data;
import lombok.NoArgsConstructor;
import saleson.common.enumeration.CompanyType;
import saleson.common.enumeration.CounterStatus;
import saleson.common.enumeration.PriorityType;
import saleson.common.enumeration.RefurbishmentStatus;

@Data
@NoArgsConstructor
public class AlrEolItem {
	private Long id;
	private RefurbishmentStatus refurbishmentStatus;//refurbishmentStatus
	private PriorityType priority;
	private String creationDateTime;//Alert Date

	private Long moldId;
	private String moldCode;
	private ToolingStatus toolingStatus;

	private Long counterId;
	private String counterCode;
	private CounterStatus sensorStatus;

	private Integer accumShotCount;
//	private String lastShotDateTime;
	private Integer estimateExtendedLife;
	private String endLifeDate;

	private Long companyId;
	private String companyName;
	private String companyCode;
	private CompanyType companyType;

	private Long locationId;
	private String locationName;
	private String locationCode;

	@QueryProjection
	public AlrEolItem(Long id, RefurbishmentStatus refurbishmentStatus, PriorityType priority, Instant createdAt, //
			Long moldId, String moldCode, ToolingStatus toolingStatus, //
			Long counterId, String counterCode, String sensorStatus, //
			Integer accumShotCount, Integer estimateExtendedLife, Instant endLifeAt, //
			Long companyId, String companyName, String companyCode, CompanyType companyType, //
			Long locationId, String locationName, String locationCode//
	) {
		String zoneId = LocationUtils.getZoneIdByLocationId(locationId);

		this.id = id;
		this.priority = priority;
		this.refurbishmentStatus = refurbishmentStatus;
		this.creationDateTime = DateUtils2.format(createdAt, DateUtils2.DatePattern.yyyy_MM_dd_HH_mm_ss, zoneId);

		this.moldId = moldId;
		this.moldCode = moldCode;
		this.toolingStatus = toolingStatus;

		this.counterId = counterId;
		this.counterCode = counterCode;
		this.sensorStatus = sensorStatus == null ? null : CounterStatus.valueOf(sensorStatus);

		this.accumShotCount = accumShotCount;
		this.estimateExtendedLife = estimateExtendedLife;
		this.endLifeDate = DateUtils2.format(endLifeAt, DateUtils2.DatePattern.yyyy_MM_dd, zoneId);

		this.companyId = companyId;
		this.companyName = companyName;
		this.companyCode = companyCode;
		this.companyType = companyType;

		this.locationId = locationId;
		this.locationName = locationName;
		this.locationCode = locationCode;
	}
}
