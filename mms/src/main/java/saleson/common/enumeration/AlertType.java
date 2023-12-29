package saleson.common.enumeration;

import saleson.common.enumeration.mapper.CodeMapperType;

public enum AlertType implements CodeMapperType {
	// Mold Alert
	RELOCATION("Relocation"),
	DISCONNECTED("Disconnected"),
	CYCLE_TIME("Cycle Time"),
	MAINTENANCE("Preventive Maintenance"),
	CORRECTIVE_MAINTENANCE("Corrective Maintenance"),
	EFFICIENCY("Efficiency"),
	MISCONFIGURE("Misconfigure"),
	DATA_SUBMISSION("Data Submission"),
	DOWNTIME_MACHINE("Downtime Machine"),

	// Admin Alert
	TERMINAL_DISCONNECTED("Disconnected"),			// 연결 끊긴 터미널 목록
	COUNTER_MISWORKING("Counter Misworking"),		// 금형과 연결이 안된 채 동작하거나 등록이 안된채 동작하는 카운터 목록
	TOOL_MISWORKING("Tool Misworking")	,			// 카운터가 인스톨 되었는데 정상적으로 인지가 안되는 금형 리스트
	REFURBISHMENT("Refurbishment"),
	DETACHMENT("Detachment"),
	DOWNTIME("Downtime")
	;

	private String title;

	AlertType(String title) {
		this.title = title;
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
		return true;
	}

}
