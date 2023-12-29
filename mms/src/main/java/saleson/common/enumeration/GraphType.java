package saleson.common.enumeration;

import saleson.common.enumeration.mapper.CodeMapperType;

public enum GraphType implements CodeMapperType {
	QUICK_STATS("Quick Fact", true), //
	DISTRIBUTION("Tooling Distribution", true), //
	HIERARCHY("Project Hierarchy", true), //
	CYCLE_TIME_STATUS("Cycle Time Status", true), //
	CAPACITY("Capacity Utilization", false), //
	TOOLING("Tooling Status", true), //
	DOWNTIME("Downtime", true), //
	OEE("Overall Equipment Effectiveness", false), //
	OTE("Overall Tooling Effectiveness", true), //
	MAINTENANCE_STATUS("Preventive Maintenance", true), //
	PRODUCTION_RATE("Production Quantity", true), //
	UTILIZATION_RATE("Utilization Rate", true), //
	UPTIME_STATUS("Uptime Status", true), //
	MAXIMUM_CAPACITY("Production Capacity", true), //
	IMPLEMENTATION_STATUS("Implementation Status", false), //
	CYCLE_TIME("Cycle Time", true), //
	ON_TIME_DELIVERY("On-Time Delivery", true), //
	PRODUCTION_PATTERN_ANALYSIS("Production Pattern Analysis", true), //
	ACTUAL_TARGET_UPTIME_RATIO("Tooling Uptime Ratio", true), //
	DATA_COMPLETION_RATE("Date Completion Rate", false), //
	INACTIVE_TOOLINGS("Inactive Toolings", true), //
	POA("Purchase Order Assessment", false);
//    COMMON_PARTS("Common Parts / Individual Parts", true),

//    TOOLING_COST("Total Tooling Cost", true),
//    RECENT_ACTIVITIES("Recent Activities", true),
//    DEVICE_INSTALLATION("Device Installation", true),
//    MAINTENANCE_EFFECTIVENESS("Maintenance Effectiveness", true),
//    SUPPORTING_DOCUMENT("Supporting Document", true);

	private String title;
	private Boolean enabled;

	GraphType(String title, boolean enabled) {
		this.title = title;
		this.enabled = enabled;

	}

	@Override
	public String getCode() {
		return name();
	}

	@Override
	public String getTitle() {
		return title;
	}

	@Override
	public String getDescription() {
		return "";
	}

	@Override
	public Boolean isEnabled() {
		return enabled;
	}

	public Integer getPosition() {
		if (this.equals(QUICK_STATS)) {
			return 1;
		} else if (this.equals(DISTRIBUTION)) {
			return 2;
		} else if (this.equals(HIERARCHY)) {
			return 3;
		} else if (this.equals(CYCLE_TIME_STATUS)) {
			return 4;
		} else if (this.equals(CAPACITY)) {
			return 5;
		} else if (this.equals(TOOLING)) {
			return 6;
		} else if (this.equals(DOWNTIME)) {
			return 7;
		} else if (this.equals(OTE)) {
			return 8;
		} else if (this.equals(MAINTENANCE_STATUS)) {
			return 9;
		} else if (this.equals(PRODUCTION_RATE)) {
			return 10;
		} else if (this.equals(UTILIZATION_RATE)) {
			return 11;
		} else if (this.equals(UPTIME_STATUS)) {
			return 12;
		} else if (this.equals(MAXIMUM_CAPACITY)) {
			return 13;
		} else if (this.equals(CYCLE_TIME)) {
			return 14;
		} else if (this.equals(ON_TIME_DELIVERY)) {
			return 15;
		} else if (this.equals(ACTUAL_TARGET_UPTIME_RATIO)) {
			return 16;
		} else if (this.equals(PRODUCTION_PATTERN_ANALYSIS)) {
			return 17;
//        } else if(this.equals(OEE)) return 18;
		} else if (this.equals(DATA_COMPLETION_RATE)) {
			return 19;
		} else if (this.equals(INACTIVE_TOOLINGS)) {
			return 20;
		}
		return 0;
	}
}
