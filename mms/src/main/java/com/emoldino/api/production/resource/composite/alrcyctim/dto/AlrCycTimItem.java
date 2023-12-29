package com.emoldino.api.production.resource.composite.alrcyctim.dto;

import com.emoldino.api.asset.resource.base.mold.enumeration.ToolingStatus;
import com.emoldino.api.common.resource.base.location.util.LocationUtils;
import com.emoldino.framework.util.DateUtils2;
import lombok.Data;
import lombok.NoArgsConstructor;
import saleson.common.enumeration.CompanyType;
import saleson.common.enumeration.CounterStatus;
import saleson.common.enumeration.CycleTimeStatus;
import saleson.common.enumeration.OperatingStatus;
import saleson.common.enumeration.NotificationStatus;

import java.time.Instant;

@Data
@NoArgsConstructor
public class AlrCycTimItem {
    private Long id;

    private NotificationStatus alertStatus; // Status
    private String creationDateTime;
    private Integer contractedCycleTime; // Final Approved CT

    private Double cycleTime;
    private CycleTimeStatus cycleTimeStatus;
    private Double variance;

    private Long moldId;
    private String moldCode;
    private ToolingStatus toolingStatus;
    private OperatingStatus operatingStatus;
    private CounterStatus sensorStatus;

    private Long companyId;
    private String companyName;
    private String companyCode;
    private CompanyType companyType;

    private Long locationId;
    private String locationCode;
    private String locationName;

    public AlrCycTimItem( //
            Long id, //
            NotificationStatus alertStatus, Instant creationDateTime, Integer contractedCycleTime, //
            Double cycleTime, CycleTimeStatus cycleTimeStatus, Double variance, //
            Long moldId, String moldCode, ToolingStatus toolingStatus, OperatingStatus operatingStatus, String sensorStatus, //
            Long companyId, String companyName, String companyCode, CompanyType companyType, //
            Long locationId, String locationCode, String locationName //
    ) {

        String zoneId = LocationUtils.getZoneIdByLocationId(locationId);

        this.id = id;

        this.alertStatus = alertStatus;
        this.creationDateTime = DateUtils2.format(creationDateTime, DateUtils2.DatePattern.yyyy_MM_dd_HH_mm_ss, zoneId);
        this.contractedCycleTime = contractedCycleTime;
        this.cycleTime = cycleTime;
        this.cycleTimeStatus = cycleTimeStatus;
        this.variance = variance;

        this.moldId = moldId;
        this.moldCode = moldCode;
        this.toolingStatus = toolingStatus;
        this.operatingStatus = operatingStatus;
        this.sensorStatus = sensorStatus == null ? null : CounterStatus.valueOf(sensorStatus);

        this.companyId = companyId;
        this.companyName = companyName;
        this.companyCode = companyCode;
        this.companyType = companyType;

        this.locationId = locationId;
        this.locationCode = locationCode;
        this.locationName = locationName;
    }
}
