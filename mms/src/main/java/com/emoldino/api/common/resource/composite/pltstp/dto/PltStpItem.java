package com.emoldino.api.common.resource.composite.pltstp.dto;

import java.util.List;

import com.emoldino.api.common.resource.base.location.util.LocationUtils;
import com.emoldino.api.common.resource.base.object.dto.FieldValue;
import com.emoldino.framework.util.DateUtils2;
import com.emoldino.framework.util.ValueUtils;

import lombok.Data;
import lombok.NoArgsConstructor;
import saleson.model.Location;

@Data
@NoArgsConstructor
public class PltStpItem {
    private Long id;
    private String name;
    private String locationCode;
    private boolean enabled;
    private String address;
	private String timeZoneId;
	private String memo;

    private Long companyId;
    private String companyName;
    private String companyCode;
    private Boolean companyEnabled;

    private long terminalCount;

    private String firstAreaName;
    private List<PltStpArea> areas;
    private String createdAtDate;

    private List<FieldValue> customFields;

    public PltStpItem(Location location, Object terminalCount, String firstAreaName) {
        ValueUtils.map(location, this);

        this.companyName = location.getCompany() == null ? null : location.getCompany().getName();
        this.companyCode = location.getCompany() == null ? null : location.getCompany().getCompanyCode();
        this.companyEnabled = location.getCompany() != null && location.getCompany().isEnabled();

        this.terminalCount = terminalCount != null ? Long.parseLong(terminalCount.toString()) : 0L;
        this.firstAreaName = firstAreaName;

        this.createdAtDate = DateUtils2.format(location.getCreatedAt(),
                DateUtils2.DatePattern.yyyy_MM_dd,
                LocationUtils.getZoneIdByLocation(location));
    }
}
