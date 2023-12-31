package com.emoldino.api.common.resource.base.noti.enumeration;

public enum NotiCode {

	WO_CREATED, //
	WO_DECLINED, //
	WO_CANCELLED, //
	WO_ACCEPTED, //
	WO_REOPENED, //
	WO_CRT_APPR_REQUESTED, //
	WO_CRT_APPROVED, //
	WO_CRT_DECLINED, //
	WO_CPT_APPR_REQUESTED, //
	WO_CPT_APPROVED, //
	WO_CPT_CANCELLED, //
	WO_CPT_REJECTED, //
	WO_DISAPPROVED, //
	WO_MOD_APPR_REQUESTED, //
	WO_MOD_APPROVED, //
	WO_MOD_REJECTED, //
	WO_COMPLETED, //
//	WO_MINE_COMPLETED, //

	NOTE_MENTIONED, //
	NOTE_REPLIED,

	DR_REQUESTED, //
	DR_DEADLINE_UPCOMING, //
	DR_OVERDUE, //
	DR_CANCELED, //
	DR_DECLINED, //
	DR_COMPLETED, //
	DR_REOPENED, //

	// Alert Center
//	DISCONNECTED, //
	TOOLING_DISCONNECTED, //
	TERMINAL_DISCONNECTED, //
//	RELOCATION, //
	TOOLING_RELOCATED, //
//	CYCLE_TIME, //
	TOOLING_CYCLE_TIME,//
//	UPTIME
	TOOLING_UPTIME, //
//	DOWNTIME, //
	TOOLING_DOWNTIME_L1, //
	TOOLING_DOWNTIME_L2, //
//	DOWNTIME_MACHINE, //
	MACHINE_DOWNTIME, //
//	MISCONFIGURE, //
	TOOING_RESET, //
//	MAINTENANCE, //
	TOOLING_MAINT_UPCOMING, //
	TOOLING_MAINT_OVERDUE, //
//	REFURBISHMENT, //
	TOOLING_REFURBISHMENT,
//	DETACHMENT, //
	SENSOR_DETACHED, //

//	EFFICIENCY, //
//	DATA_SUBMISSION, //

	USER_ACCESS_REQUESTED //

//	SUPPORT_TICKET_CHANGED, //

}
