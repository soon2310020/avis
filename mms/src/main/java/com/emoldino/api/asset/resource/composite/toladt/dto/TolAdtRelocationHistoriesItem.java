package com.emoldino.api.asset.resource.composite.toladt.dto;

import com.emoldino.api.common.resource.base.location.util.LocationUtils;
import com.emoldino.framework.util.DateUtils2;
import com.emoldino.framework.util.DateUtils2.DatePattern;
import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import saleson.common.enumeration.CompanyType;
import saleson.model.MoldLocation;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TolAdtRelocationHistoriesItem {

	@JsonIgnore
	private MoldLocation moldLocation;

	public String getRelocatedDateTime() {
		return moldLocation == null || moldLocation.getNotificationAt() == null ? null//
				: DateUtils2.format(moldLocation.getNotificationAt(), DatePattern.yyyy_MM_dd_HH_mm_ss, LocationUtils.getZoneIdByMold(moldLocation.getMold()));
	}

	public String getRelocationStatus() {
		return moldLocation == null || moldLocation.getMoldLocationStatus() == null ? null//
				: moldLocation.getMoldLocationStatus().getCode();
	}

	public Long getCompanyId() {
		return moldLocation == null || moldLocation.getLocation() == null ? null//
				: moldLocation.getLocation().getCompanyId();
	}

	public String getCompanyName() {
		return moldLocation == null || moldLocation.getLocation() == null ? null//
				: moldLocation.getLocation().getCompanyName();
	}

	public String getCompanyCode() {
		return moldLocation == null || moldLocation.getLocation() == null ? null//
				: moldLocation.getLocation().getCompanyCode();
	}

	public CompanyType getCompanyType() {
		return moldLocation == null || moldLocation.getLocation() == null || moldLocation.getLocation().getCompany() == null ? null//
				: moldLocation.getLocation().getCompany().getCompanyType();
	}

	public Long getLocationId() {
		return moldLocation == null ? null//
				: moldLocation.getLocationId();
	}

	public String getLocationName() {
		return moldLocation == null || moldLocation.getLocation() == null ? null//
				: moldLocation.getLocation().getName();
	}

	public String getLocationCode() {
		return moldLocation == null || moldLocation.getLocation() == null ? null//
				: moldLocation.getLocation().getLocationCode();
	}

	public Long getAreaId() {
		return moldLocation == null ? null//
				: moldLocation.getAreaId();
	}

	public String getAreaName() {
		return moldLocation == null || moldLocation.getArea() == null ? null//
				: moldLocation.getArea().getName();
	}

//	public Long getPreviousCompanyId() {
//		return moldLocation == null || moldLocation.getPreviousLocation() == null ? null //
//				: moldLocation.getPreviousLocation().getCompanyId();
//	}
//
//	public String getPreviousCompanyName() {
//		return moldLocation == null || moldLocation.getPreviousLocation() == null ? null //
//				: moldLocation.getPreviousLocation().getCompanyName();
//	}
//
//	public String getPreviousCompanyCode() {
//		return moldLocation == null || moldLocation.getPreviousLocation() == null ? null //
//				: moldLocation.getPreviousLocation().getCompanyCode();
//	}
//
//	public Long getPreviousLocationId() {
//		return moldLocation.getPreviousLocationId();
//	}
//
//	public String getPreviousLocationName() {
//		return moldLocation == null || moldLocation.getPreviousLocation() == null ? null //
//				: moldLocation.getPreviousLocation().getName();
//	}
//
//	public String getPreviousLocationCode() {
//		return moldLocation == null || moldLocation.getPreviousLocation() == null ? null //
//				: moldLocation.getPreviousLocation().getLocationCode();
//	}
//
//	public Long getPreviousAreaId() {
//		return moldLocation.getPreviousAreaId();
//	}
//
//	public String getPreviousAreaName() {
//		return moldLocation == null || moldLocation.getPreviousArea() == null ? null //
//				: moldLocation.getPreviousArea().getName();
//	}

}
