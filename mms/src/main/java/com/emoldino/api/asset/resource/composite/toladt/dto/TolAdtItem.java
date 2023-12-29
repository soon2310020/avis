package com.emoldino.api.asset.resource.composite.toladt.dto;

import java.math.BigDecimal;
import java.time.Instant;

import com.emoldino.api.asset.resource.base.mold.enumeration.ToolingStatus;
import com.emoldino.api.common.resource.base.accesscontrol.annotation.DataLeakDetector;
import com.emoldino.framework.util.DateUtils2;
import com.emoldino.framework.util.DateUtils2.DatePattern;
import com.emoldino.framework.util.DateUtils2.Zone;
import com.emoldino.framework.util.ValueUtils;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.querydsl.core.annotations.QueryProjection;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class TolAdtItem {
	private Long id;
	private String moldCode;
	private ToolingStatus toolingStatus;

	private Long supplierId;
	private String supplierName;
	private String supplierCode;

	private Long companyId;
	private String companyName;
	private String companyCode;

	@DataLeakDetector(disabled = true)
	private Long locationId;
	private String locationName;
	private String locationCode;
	@JsonIgnore
	private String zoneId;

	private Long areaId;
	private String areaName;

	private Double utilizationRate;

	@JsonIgnore
	private Instant relocatedAt;

	public BigDecimal getUtilizationRate() {
		return ValueUtils.toBigDecimal(utilizationRate, null);
	}

	public String getRelocatedDateTime() {
		return DateUtils2.format(relocatedAt, DatePattern.yyyy_MM_dd_HH_mm_ss, ValueUtils.toString(zoneId, Zone.SYS));
	}

	@QueryProjection
	public TolAdtItem(//
			Long id, String moldCode, ToolingStatus toolingStatus, //
			Long supplierId, String supplierName, String supplierCode, //
			Long companyId, String companyName, String companyCode, //
			Long locationId, String locationName, String locationCode, String zoneId, //
			Long areaId, String areaName, //
			Double utilizationRate, //
			Instant relocatedAt//
	) {
		this.id = id;
		this.moldCode = moldCode;
		this.toolingStatus = toolingStatus;

		this.supplierId = supplierId;
		this.supplierName = supplierName;
		this.supplierCode = supplierCode;

		this.companyId = companyId;
		this.companyName = companyName;
		this.companyCode = companyCode;

		this.locationId = locationId;
		this.locationName = locationName;
		this.locationCode = locationCode;
		this.zoneId = zoneId;

		this.areaId = areaId;
		this.areaName = areaName;

		this.utilizationRate = utilizationRate;

		this.relocatedAt = relocatedAt;
	}

}
