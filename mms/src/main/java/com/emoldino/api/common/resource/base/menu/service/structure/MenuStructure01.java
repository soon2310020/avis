package com.emoldino.api.common.resource.base.menu.service.structure;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.emoldino.api.common.resource.base.menu.dto.MenuItem;
import com.emoldino.api.common.resource.base.menu.dto.MenuTreeNode;

public class MenuStructure01 implements MenuStructure {

	@Override
	public List<MenuTreeNode> get() {
		List<MenuTreeNode> nodes = new ArrayList<>();
		nodes.add(//
				new MenuTreeNode("Overview", "overview", //
						new MenuTreeNode("CM9010", "Overview", "overview", "20221008", true, //
								"/front/tabbed-overview", "/images/icon/tabbed-overview/tabbed-overview.svg", //
								true, true, true, true, //
								true, Arrays.asList(), null), //
						new MenuTreeNode("CM9020", "Dashboard", "dashboard", "20221008", //
								"/", "/images/icon/dashboard.svg", //
								true, true, true, true, //
								true, true, Arrays.asList(), null, //
								// PR8010
								new MenuTreeNode("DASHBOARD-CYCLE_TIME", "Cycle Time", "cycle_time", "SUBFUNCTION", "20221220", //
										true, true, true, true, //
										false, Arrays.asList("OEM_REGULAR", "SUPPLIER_REGULAR"), null), //
								// PR8020
								new MenuTreeNode("DASHBOARD-CYCLE_TIME_STATUS", "Cycle Time Status", "cycle_time_status", "SUBFUNCTION", "20221220", //
										true, true, true, true, //
										false, Arrays.asList("OEM_REGULAR", "SUPPLIER_REGULAR"), null), //
								// AS8010
								new MenuTreeNode("DASHBOARD-DISTRIBUTION", "Distribution of Tooling", "distribution_of_tooling", "SUBFUNCTION", "20221220", //
										true, true, true, true, //
										false, Arrays.asList("OEM_REGULAR", "SUPPLIER_REGULAR"), null), //
								// AS8030
								new MenuTreeNode("DASHBOARD-INACTIVE_TOOLINGS", "Inactive Toolings", "inactive_toolings", "SUBFUNCTION", "20221220", //
										true, true, true, true, //
										false, Arrays.asList("OEM_REGULAR", "SUPPLIER_REGULAR"), null), //
//								// SC8010
//								new MenuTreeNode("DASHBOARD-ON_TIME_DELIVERY", "On-Time Delivery", "on_time_delivery", "SUBFUNCTION", "20221220", //
//										true, true, true, false, //
//										false, Arrays.asList("OEM_REGULAR"), null), //
								// AS8040
								new MenuTreeNode("DASHBOARD-MAINTENANCE_STATUS", "Preventive Maintenance", "preventive_maintenance", "SUBFUNCTION", "20221220", //
										true, true, true, true, //
										false, Arrays.asList("OEM_REGULAR", "SUPPLIER_REGULAR"), null), //
								// PR8040
								new MenuTreeNode("DASHBOARD-MAXIMUM_CAPACITY", "Production Capacity", "maximum_capacity", "SUBFUNCTION", "20221220", //
										true, true, true, true, //
										false, Arrays.asList("OEM_REGULAR", "SUPPLIER_REGULAR"), null), //
								// SC8020
								new MenuTreeNode("DASHBOARD-PRODUCTION_PATTERN_ANALYSIS", "Production Pattern Analysis", "production_pattern_analysis", "SUBFUNCTION", "20221220", //
										true, true, true, false, //
										false, Arrays.asList("OEM_REGULAR"), null), //
								// PR8050
								new MenuTreeNode("DASHBOARD-PRODUCTION_RATE", "Production Quantity", "production_rate", "SUBFUNCTION", "20221220", //
										true, true, true, true, //
										false, Arrays.asList("OEM_REGULAR", "SUPPLIER_REGULAR"), null), //
								// SC8040
								new MenuTreeNode("DASHBOARD-QUICK_STATS", "Quick Fact", "quick_fact", "SUBFUNCTION", "20221220", //
										true, true, true, true, //
										false, Arrays.asList("OEM_REGULAR", "SUPPLIER_REGULAR"), null), //
								// AS8050
								new MenuTreeNode("DASHBOARD-TOOLING", "Tooling Status", "tooling_status", "SUBFUNCTION", "20221220", //
										true, true, true, true, //
										false, Arrays.asList("OEM_REGULAR", "SUPPLIER_REGULAR"), null), //
								// AS8060
								new MenuTreeNode("DASHBOARD-ACTUAL_TARGET_UPTIME_RATIO", "Tooling Uptime Ratio", "actual_target_uptime_ratio", "SUBFUNCTION", "20221220", //
										true, true, true, true, //
										false, Arrays.asList("OEM_REGULAR", "SUPPLIER_REGULAR"), null), //
								// AS8080
								new MenuTreeNode("DASHBOARD-UTILIZATION_RATE", "Utilization Rate", "utilization_rate", "SUBFUNCTION", "20221220", //
										true, true, true, true, //
										false, Arrays.asList("OEM_REGULAR", "SUPPLIER_REGULAR"), null), //
								// AS8070
								new MenuTreeNode("DASHBOARD-UPTIME_STATUS", "Uptime Status", "uptime_status", "SUBFUNCTION", "20221220", //
										true, true, true, true, //
										false, Arrays.asList("OEM_REGULAR", "SUPPLIER_REGULAR"), null)//
//							    HIERARCHY("Project Hierarchy", true),
//							    CAPACITY("Capacity Utilization", false),
//							    DOWNTIME("Downtime", true),
//							    OEE("Overall Equipment Effectiveness", false),
//							    OTE("Overall Tooling Effectiveness", true),
//							    IMPLEMENTATION_STATUS("Implementation Status", false),
//							    DATA_COMPLETION_RATE("Date Completion Rate", false),
//							    POA("Purchase Order Assessment", false);
//								ON_TIME_DELIVERY
						), //
						new MenuTreeNode("CM9030", "Alert Center", "alert_center", "20221109", //
								"/front/alert-center", "/images/icon/alert-icon.svg", //
								true, true, true, true, //
								false, Arrays.asList("OEM_REGULAR", "SUPPLIER_REGULAR", "SUPPLIER_INSTALLATION"), //
								null, //
								// CM9030-01
								new MenuTreeNode("ALERT-RELOCATION", "Relocation", "relocation", "SUBFUNCTION", "20221109", //
										"/front/alert-center#relocation", "/images/icon/alert/relocation-icon-hover.svg", //
										true, true, true, true, //
										false, Arrays.asList("OEM_REGULAR", "SUPPLIER_REGULAR"), //
										Arrays.asList(//
												new MenuItem("btnAlertConfirm", "Confirm (Alert)", "alert_confirm", "cta-button")//
										)), //
								// CM9030-02
								new MenuTreeNode("ALERT-DISCONNECTED", "Disconnection", "disconnection", "SUBFUNCTION", "20221109", //
										"/front/alert-center#disconnection", "/images/icon/alert/disconnection-icon-hover.svg", //
										true, true, true, false, //
										false, Arrays.asList("OEM_REGULAR", "SUPPLIER_REGULAR", "SUPPLIER_INSTALLATION"), //
										Arrays.asList(//
												new MenuItem("btnTerminalConfirm", "Confirm (Terminal)", "terminal_confirm", "cta-button"), //
												new MenuItem("btnToolingConfirm", "Confirm (Tooling)", "tooling_confirm", "cta-button")//
										)), //
								// CM9030-03
								new MenuTreeNode("ALERT-CYCLE_TIME", "Cycle Time", "cycle_time", "SUBFUNCTION", "20221109", //
										"/front/alert-center#cycle-time", "/images/icon/alert/cycle-time-icon-hover.svg", //
										true, true, true, false, //
										false, Arrays.asList("OEM_REGULAR", "SUPPLIER_REGULAR"), //
										Arrays.asList(//
												new MenuItem("btnAlertConfirm", "Confirm (Alert)", "alert_confirm", "cta-button"), //
												new MenuItem("btnOutsideL1Confirm", "Confirm (Outside L1)", "outside_l1_confirm", "cta-button"), //
												new MenuItem("btnOutsideL2Confirm", "Confirm (Outside L2)", "outside_l2_confirm", "cta-button")//
										)), //
								// CM9030-04
								new MenuTreeNode("ALERT-MAINTENANCE", "Maintenance", "maintenance", "SUBFUNCTION", "20221109", //
										"/front/alert-center#maintenance", "/images/icon/alert/maintenance-icon-hover.svg", //
										true, true, true, true, //
										false, Arrays.asList("OEM_REGULAR", "SUPPLIER_REGULAR"), //
										Arrays.asList(//
												new MenuItem("btnPmConfirm", "Confirm (PM)", "pm_confirm", "cta-button"), //
												new MenuItem("btnCmRequest", "Request (CM)", "cm_request", "cta-button"), //
												new MenuItem("btnCmConfirm", "Confirm (CM)", "cm_confirm", "cta-button"), //
												new MenuItem("btnCmApprove", "Approve (CM)", "cm_approve", "cta-button"), //
												new MenuItem("btnCmDisapprove", "Disapprove (CM)", "cm_disapprove", "cta-button")//
										)), //
								// CM9030-05
								new MenuTreeNode("ALERT-EFFICIENCY", "Uptime", "uptime", "SUBFUNCTION", "20221109", //
										"/front/alert-center#uptime", "/images/icon/alert/uptime-icon-hover.svg", //
										true, true, true, false, //
										false, Arrays.asList("OEM_REGULAR", "SUPPLIER_REGULAR"), //
										Arrays.asList(//
												new MenuItem("btnAlertConfirm", "Confirm (Alert)", "alert_confirm", "cta-button"), //
												new MenuItem("btnOutsideL1Confirm", "Confirm (Outside L1)", "outside_l1_confirm", "cta-button"), //
												new MenuItem("btnOutsideL2Confirm", "Confirm (Outside L2)", "outside_l2_confirm", "cta-button")//
										)), //
								// CM9030-06
								new MenuTreeNode("ALERT-MISCONFIGURE", "Reset", "reset", "SUBFUNCTION", "20221109", //
										"/front/alert-center#reset", "/images/icon/alert/reset-icon-hover.svg", //
										true, true, true, false, //
										false, Arrays.asList("OEM_REGULAR", "SUPPLIER_REGULAR", "SUPPLIER_INSTALLATION"), null), //
								// CM9030-07
								new MenuTreeNode("ALERT-DATA_SUBMISSION", "Data Approval", "data_approval", "SUBFUNCTION", "20221109", //
										"/front/alert-center#data-approval", "/images/icon/alert/data-approval-icon-hover.svg", //
										true, true, true, true, //
										false, Arrays.asList("OEM_REGULAR"), null), //
								// CM9030-08
								new MenuTreeNode("ALERT-REFURBISHMENT", "Refurbishment", "refurbishment", "SUBFUNCTION", "20221109", //
										"/front/alert-center#refurbishment", "/images/icon/alert/refurbishment-icon-hover.svg", //
										true, true, true, true, //
										false, Arrays.asList("OEM_REGULAR", "SUPPLIER_REGULAR"), null), //
								// CM9030-09
								new MenuTreeNode("ALERT-DETACHMENT", "Detachment", "detachment", "SUBFUNCTION", "20221109", //
										"/front/alert-center#detachment", "/images/icon/alert/detachment-icon-hover.svg", //
										true, true, true, true, //
										false, Arrays.asList("OEM_REGULAR", "SUPPLIER_REGULAR", "SUPPLIER_INSTALLATION"), null), //
								// CM9030-10
								new MenuTreeNode("ALERT-DOWNTIME", "Tooling Downtime", "tooling_downtime", "SUBFUNCTION", "20221109", //
										"/front/alert-center#downtime", "/images/icon/alert/downtime-icon-hover.svg", //
										true, true, true, false, //
										false, Arrays.asList("OEM_REGULAR", "SUPPLIER_REGULAR"), null), //
								// CM9030-11
								new MenuTreeNode("ALERT-DOWNTIME_MACHINE", "Machine Downtime", "machine_downtime", "SUBFUNCTION", "20221109", //
										"/front/alert-center#machine-downtime", "/images/icon/alert/downtime-icon-hover.svg", //
										true, true, true, false, //
										false, Arrays.asList("OEM_REGULAR", "SUPPLIER_REGULAR"), //
										Arrays.asList(//
												new MenuItem("btnAlertRegisterReason", "Register Reason (Alert)", "alert_register_reason", "cta-button"), //
												new MenuItem("btnAlertEditRegisteredReason", "Edit Registered Reason (Alert)", "alert_edit_registered_reason", "cta-button"), //
												new MenuItem("btnHistoryLogConfirmDowntime", "Confirm Downtime (History Log)", "history_log_confirm_downtime", "cta-button"), //
												new MenuItem("btnHistoryLogEditDowntimeReason", "Edit Downtime Reason (History Log)", "history_log_edit_downtime_reason",
														"cta-button")//
										))//
//								CORRECTIVE_MAINTENANCE("Corrective Maintenance"),
//								TERMINAL_DISCONNECTED("Disconnected"),
//								COUNTER_MISWORKING("Counter Misworking"),
//								TOOL_MISWORKING("Tool Misworking"),
						), //
						new MenuTreeNode("PR9010", "OEE Center", "oee_center", "20221008", //
								"/front/oee-center", "/images/icon/oee-icon.svg", //
								true, true, true, false, //
								false, Arrays.asList("OEM_REGULAR", "SUPPLIER_REGULAR"), //
								Arrays.asList(//
										new MenuItem("lnkOeeRiskConfiguration", "OEE Risk Configuration (link)", "oee_risk_configuration_link", "link", "CM2050")//
								), //
								new MenuTreeNode("PR9010-01", "Number of Machines", "number_of_machines", "SUBFUNCTION", "20221109", //
										true, true, true, false, //
										false, Arrays.asList("OEM_REGULAR", "SUPPLIER_REGULAR"), //
										Arrays.asList(//
												new MenuItem("lnkMachine", "Machine (Detail)", "machine_detail", "link", "AS1020")//
										)), //
								new MenuTreeNode("PR9010-02", "Machine Downtime", "machine_downtime", "SUBFUNCTION", "20221109", //
										true, true, true, false, //
										false, Arrays.asList("OEM_REGULAR", "SUPPLIER_REGULAR"), //
										Arrays.asList(//
												new MenuItem("lnkMachineDowntime", "Machine Downtime (Detail)", "machine_downtime_detail", "link", "PR9010-03")//
										)), //
								new MenuTreeNode("PR9010-03", "Machine Downtime (Detail)", "machine_downtime_detail", "SUBFUNCTION", "20221109", //
										true, true, true, false, //
										false, Arrays.asList("OEM_REGULAR", "SUPPLIER_REGULAR"), //
										Arrays.asList(//
												new MenuItem("btnRegisterReason", "Register Reason", "register_reason", "cta-button"), //
												new MenuItem("btnEditRegisteredReason", "Edit Registered Reason", "edit_registerd_reason", "cta-button"), //
												new MenuItem("btnEditEndedDowntimeReason", "Edit Ended Downtime Reason", "edit_ended_downtime_reason", "cta-button"), //
												new MenuItem("btnConfirmDowntime", "Confirm Downtime", "confirm_downtime", "cta-button")//
										)), //
								new MenuTreeNode("PR9010-04", "Parts Produced", "parts_produced", "SUBFUNCTION", "20221109", //
										true, true, true, false, //
										false, Arrays.asList("OEM_REGULAR", "SUPPLIER_REGULAR"), Arrays.asList(//
												new MenuItem("lnkPartsProduced", "Parts Produced (Detail)", "parts_produced_detail", "link", "PR9010-05"), //
												new MenuItem("btnInputRejectParts", "Input Reject Part", "input_reject_part", "cta-button")//
										)), //
								new MenuTreeNode("PR9010-05", "Parts Produced (Detail)", "parts_produced_detail", "SUBFUNCTION", "20221109", //
										true, true, true, false, //
										false, Arrays.asList("OEM_REGULAR", "SUPPLIER_REGULAR"), null), //
								new MenuTreeNode("PR9010-06", "Reject Rate", "reject_rate", "SUBFUNCTION", "20221109", //
										true, true, true, false, //
										false, Arrays.asList("OEM_REGULAR", "SUPPLIER_REGULAR"), //
										Arrays.asList(//
												new MenuItem("lnkRejectRate", "Reject Rate (Detail)", "reject_rate_detail", "link", "PR9010-07")//
										)), //
								new MenuTreeNode("PR9010-07", "Reject Rate (Detail)", "reject_rate_detail", "SUBFUNCTION", "20221109", //
										true, true, true, false, //
										false, Arrays.asList("OEM_REGULAR", "SUPPLIER_REGULAR"), null), //
								new MenuTreeNode("PR9010-08", "OEE", "oee", "SUBFUNCTION", "20221109", //
										true, true, true, false, //
										false, Arrays.asList("OEM_REGULAR", "SUPPLIER_REGULAR"), null),
								new MenuTreeNode("PR9010-09", "Machine Downtime Events", "machine_downtime_events", "SUBFUNCTION", "20221109", //
										true, true, true, false, //
										false, Arrays.asList("OEM_REGULAR", "SUPPLIER_REGULAR"), null) //
						), //
						new MenuTreeNode("DATA FAMILY", "Data_Family", //
								new MenuTreeNode("PR1010", "Parts", "parts", "20221008", //
										"/admin/parts", "/images/icon/part.svg", //
										true, true, true, false, //
										false, Arrays.asList("OEM_REGULAR", "SUPPLIER_REGULAR"), null), //
								new MenuTreeNode("AS1010", "Tooling", "tooling", "20221008", //
										"/admin/molds", "/images/icon/tooling.svg", //
										true, true, true, true, //
										false, Arrays.asList("OEM_REGULAR", "SUPPLIER_REGULAR"), null), //
//								new MenuTreeNode("AS1011", "Tooling New", "tooling_new", "20221014", //
//										"/admin/tooling", "/images/icon/tooling.svg", //
//										true, false, false, false, //
//										false, Arrays.asList(), null), //
								new MenuTreeNode("SC1010", "Category", "category", "20221008", //
										"/admin/categories", "/images/icon/Category.svg", //
										true, true, false, false, //
										false, Arrays.asList("OEM_REGULAR"), null), //
								new MenuTreeNode("CM1010", "Company", "company", "20221008", //
										"/admin/companies", "/images/icon/Company.svg", //
										true, true, true, true, //
										false, Arrays.asList("OEM_REGULAR", "SUPPLIER_REGULAR", "SUPPLIER_INSTALLATION"), null), //
								new MenuTreeNode("CM1020", "Plant", "location", "20221008", //
										"/admin/locations", "/images/icon/Location.svg", //
										true, true, true, true, //
										false, Arrays.asList("OEM_REGULAR", "SUPPLIER_REGULAR"), null),
								new MenuTreeNode("AS1020", "Machine", "machine", "20221008", //
										"/admin/machines", "/images/icon/machine.svg", //
										true, true, true, false, //
										false, Arrays.asList("OEM_REGULAR", "SUPPLIER_REGULAR"), null),
								new MenuTreeNode("AS1050", "Work Order", "work_order", "20221008", //
										"/admin/work-order", "/images/icon/work-order.svg", //
										true, true, true, true, //
										false, Arrays.asList("OEM_REGULAR", "SUPPLIER_REGULAR", "SUPPLIER_INSTALLATION"), null), //
								new MenuTreeNode("CM1070", "Data Import Center", "data_import_center", "20221109", //
										"/common/dat-imp", "/images/icon/dat-imp.svg", //
										true, false, false, false, //
										false, Arrays.asList("OEM_REGULAR", "SUPPLIER_REGULAR"), null, //
										new MenuTreeNode("IMPORT-COMPANY", "Company", "company", "SUBFUNCTION", "20230210", //
												"/front/dat-imp#company", "/images/icon/dat-imp/company.svg", //
												true, false, false, false, //
												false, Arrays.asList("OEM_REGULAR", "SUPPLIER_REGULAR"), null), //
										new MenuTreeNode("IMPORT-PLANT", "Plant", "plant", "SUBFUNCTION", "20230210", //
												"/front/dat-imp#plant", "/images/icon/dat-imp/plant.svg", //
												true, false, false, false, //
												false, Arrays.asList("OEM_REGULAR", "SUPPLIER_REGULAR"), null), //
										new MenuTreeNode("IMPORT-PART", "Part", "part", "SUBFUNCTION", "20230210", //
												"/front/dat-imp#part", "/images/icon/dat-imp/part.svg", //
												true, false, false, false, //
												false, Arrays.asList("OEM_REGULAR", "SUPPLIER_REGULAR"), null), //
										new MenuTreeNode("IMPORT-USER", "User", "user", "SUBFUNCTION", "20230210", //
												"/front/dat-imp#user", "/images/icon/dat-imp/user.svg", //
												true, false, false, false, //
												false, Arrays.asList("OEM_REGULAR", "SUPPLIER_REGULAR"), null), //
										new MenuTreeNode("IMPORT-MACHINE", "Machine", "machine", "SUBFUNCTION", "20230210", //
												"/front/dat-imp#machine", "/images/icon/dat-imp/machine.svg", //
												true, false, false, false, //
												false, Arrays.asList("OEM_REGULAR", "SUPPLIER_REGULAR"), null), //
										new MenuTreeNode("IMPORT-TOOLING", "Tooling", "tooling", "SUBFUNCTION", "20230210", //
												"/front/dat-imp#tooling", "/images/icon/dat-imp/tooling.svg", //
												true, false, false, false, //
												false, Arrays.asList("OEM_REGULAR", "SUPPLIER_REGULAR"), null)//
								)//
						), //
						new MenuTreeNode("DEVICES", "devices", //
								new MenuTreeNode("AS1030", "Terminal", "terminal", "20221008", //
										"/admin/terminals", "/images/icon/terminal.svg", //
										true, true, true, true, //
										false, Arrays.asList("OEM_REGULAR", "SUPPLIER_REGULAR", "SUPPLIER_INSTALLATION"), null),
								new MenuTreeNode("AS1040", "Sensor", "counter", "20221008", //
										"/admin/counters", "/images/icon/counter.svg", //
										true, true, true, true, //
										false, Arrays.asList("OEM_REGULAR", "SUPPLIER_REGULAR", "SUPPLIER_INSTALLATION"), null)//
						), new MenuTreeNode("SEARCH", "search", //
								new MenuTreeNode("CM9040", "Advanced Search", "advance_search", "20221008", //
										"/search", "/images/icon/search.svg", //
										true, true, false, false, //
										false, Arrays.asList("OEM_REGULAR"), null)//
						)//
				));
		nodes.add(//
				new MenuTreeNode("Reports", "reports", //
						new MenuTreeNode("SC6010", "Capacity Utilization", "capacity_utilization", "20221008", //
								"/report/capacity-utilization", "/images/icon/capacity-utilization.svg", //
								true, true, true, false, //
								false, Arrays.asList("OEM_REGULAR"), null), //
						new MenuTreeNode("SC6020", "Cycle Time", "cycle_time", "20221008", //
								"/report/cycle-time", "/images/icon/cycle_time.svg", //
								true, true, true, false, //
								false, Arrays.asList("OEM_REGULAR"), null),
						new MenuTreeNode("SC6030", "Production Efficiency", "production_efficiency", "20221008", //
								"/report/production-efficiency", "/images/icon/production-efficiency.svg", //
								true, true, false, false, //
								false, Arrays.asList("OEM_REGULAR"), null), //
						new MenuTreeNode("AS6020", "Tooling Audit", "tooling_audit", "20230410", //
								"/asset/tol-adt", "/images/icon/audit.svg", //
								true, false, false, false, //
								false, Arrays.asList(), null), //
						new MenuTreeNode("AN6040", "Life Cycle", "life_cycle", "20230410", //
								"/analysis/life-cycle", "/images/icon/life-cycle.svg", //
								true, false, false, false, //
								false, Arrays.asList(), null, //
								new MenuTreeNode("AN6040-01", "Overall Utilization", "overall_utilization", "SUBFUNCTION", "20230210", //
										"/analysis/life-cycle#ovr-utl", null, //
										true, false, false, false, //
										false, Arrays.asList(), null), //
								new MenuTreeNode("AN6040-02", "Budgeting", "budgeting", "SUBFUNCTION", "20230210", //
										"/analysis/life-cycle#bud", null, //
										true, false, false, false, //
										false, Arrays.asList(), null)//
						), //
						new MenuTreeNode("AN6010", "Process Analysis", "process_analysis", "20230410", //
								"/analysis/pro-ana", "/images/icon/process-analysis.svg", //
								true, false, false, false, //
								false, Arrays.asList(), null), //
						new MenuTreeNode("SC6040", "Demand Compliance", "demand_compliance", "20230410", //
								"/supplychain/dem-cpl", "/images/icon/demand-compliance.svg", //
								true, false, false, false, //
								false, Arrays.asList(), null), //
						new MenuTreeNode("AN6030", "Quality", "quality", "20230410", //
								"/analysis/quality", "/images/icon/quality_management.svg", //
								true, false, false, false, //
								false, Arrays.asList(), null, //
								new MenuTreeNode("AN6030-01", "Cycle Time Deviation", "cycle_time_deviation", "SUBFUNCTION", "20230210", //
										"/analysis/quality#cyc-tim-dev", null, //
										true, false, false, false, //
										false, Arrays.asList(), null), //
								new MenuTreeNode("AN6030-02", "Cycle Time Fluctuation", "cycle_time_fluctuation", "SUBFUNCTION", "20230210", //
										"/analysis/quality#cyc-tim-flu", null, //
										true, false, false, false, //
										false, Arrays.asList(), null)//
						) //
				));
		nodes.add(//
				new MenuTreeNode("Insights", "insights", //
						new MenuTreeNode("INSIGHTS", "insights", //
								new MenuTreeNode("AS7010", "End of life Cycle", "end_of_life_cycle", "20221008", //
										"/insight", "/images/icon/end_of_life_cycle.svg", //
										true, true, true, true, //
										false, Arrays.asList("OEM_REGULAR"), null), //
								new MenuTreeNode("AS7020", "Predictive Maintenance", "predictive_maintenance", "20221008", //
										"/insight/predictive-maintenance", "/images/icon/predictive_maintenance.svg", //
										false, true, true, true, //
										false, Arrays.asList(), null), //
								new MenuTreeNode("SC7010", "Delivery Forecast", "delivery_forecast", "20221008", //
										"/insight/delivery-forecast", "/images/icon/delivery-forecast.svg", //
										false, true, false, false, //
										false, Arrays.asList(), null),
								new MenuTreeNode("SC7020", "Quality Management", "quality_management", "20221008", //
										"/insight/quality-management", "/images/icon/quality_management.svg", //
										false, true, true, false, //
										false, Arrays.asList(), null)//
						)//
				));
		nodes.add(//
				new MenuTreeNode("AI Hub", "aihub", //
						new MenuTreeNode("AI9010", "AI Hub", "aihub", "20221008", //
								"/aihub/dashboard", null, //
								true, false, false, false, //
								false, null, null) //
				));
		nodes.add(//
				new MenuTreeNode("Administration", "administration", //
						new MenuTreeNode("CM2011", "Profile", "profile", "20221008", //
								"/profile/account", "/images/icon/profile.svg", //
								true, true, true, true, //
								true, Arrays.asList("OEM_REGULAR"), null), //
						new MenuTreeNode("GENERAL SETTINGS", "general_setting", //
								new MenuTreeNode("CM2010", "User", "user", "20221008", //
										"/admin/users", "/images/icon/User.svg", //
										true, true, true, true, //
										false, Arrays.asList("OEM_REGULAR"), null), //
								new MenuTreeNode("CM2020", "Access Control", "access_control", "20221008", //
										"/admin/roles", "/images/icon/access_control.svg", //
										true, false, false, false, //
										false, null, null), //
								new MenuTreeNode("CM2030", "Role-User", "role_config", "20221008", //
										"/admin/role-config", "/images/icon/person-add-icon.svg", //
										true, true, true, true, //
										false, Arrays.asList("OEM_REGULAR"), null), //
//								new MenuTreeNode("CM2040", "Permission Configuration", "permission_configuration", "20221008", //
//										"/admin/permission-config", "/images/icon/access_control.svg", //
//										true, false, false, false, //
//										false, null, null), //
								new MenuTreeNode("CM2041", "Menu Permission", "menu_permission_config", "20221109", //
										"/admin/permission-config-new", "/images/icon/access_control.svg", //
										true, false, false, false, //
										false, null, null), //

								new MenuTreeNode("CM2050", "Configuration", "configuration", "20221008", //
										"/admin/configuration", "/images/icon/Configuration.svg", //
										true, true, false, false, //
										false, null, null), //

								new MenuTreeNode("CM1080", "File Management Center", "file_management_center", "20221109", //
										"/common/fle-mng", "/images/icon/file-management.svg", //
										true, false, false, false, //
										false, null, null), //
								new MenuTreeNode("CM2060", "Language", "language", "20221008", //
										"/admin/time-language", "/images/icon/time_language.svg", //
										true, true, true, true, //
										false, Arrays.asList("OEM_REGULAR", "SUPPLIER_REGULAR", "SUPPLIER_INSTALLATION"), null),
								new MenuTreeNode("CM2070", "Currency", "currency", "20221008", //
										"/admin/currency", "/images/icon/Currency.svg", //
										true, true, true, true, //
										false, null, null) //
						), //
						new MenuTreeNode("MASTER DATA", "master_data", //
//								new MenuTreeNode("CM1030", "Data Registration", "Data_Registration", "20221008", //
//										"/admin/data-registration", "/images/icon/data-registration.svg", //
//										true, true, true, true, //
//										false, Arrays.asList("OEM_REGULAR", "SUPPLIER_REGULAR"), null), //
								new MenuTreeNode("CM1035", "Data Request", "Data_Request", "20221227", //
										"/admin/data-requests", "/images/icon/data-requests.svg", //
										true, true, true, true, //
										false, Arrays.asList("OEM_REGULAR", "SUPPLIER_REGULAR"), null), //
//								new MenuTreeNode("CM1040", "Data Completion", "Data_Completion", "20221008", //
//										"/admin/data-completion", "/images/icon/data-completion.svg", //
//										true, true, true, true, //
//										false, Arrays.asList("OEM_REGULAR", "SUPPLIER_REGULAR"), null), //
								new MenuTreeNode("CM1050", "List Management", "list_management", "20221008", //
										"/admin/checklist-center", "/images/icon/maintenance_checklist.svg", //
										true, true, true, true, //
										false, Arrays.asList("OEM_REGULAR", "SUPPLIER_REGULAR"), null),
								new MenuTreeNode("CM1060", "Codelist Center", "codelist_center", "20221008", //
										"/admin/codelist-center", "/images/icon/codelist-center.svg", //
										true, false, false, false, //
										false, Arrays.asList(), null)//
						), //
						new MenuTreeNode("OPERATION DATA", "operation_data", //
								new MenuTreeNode("PR2010", "Reject Rate", "reject_rate", "20221008", //
										"/admin/reject-rates", "/images/icon/reject_rate.svg", //
										true, true, true, false, //
										false, Arrays.asList("OEM_REGULAR", "SUPPLIER_REGULAR"), null)//,
//								new MenuTreeNode("PR2020", "Machine Availability", "Machine_Availability", "20221008", //
//										"/admin/machine-availability", "/images/icon/machine-availability.svg", //
//										true, true, true, false, //
//										false, Arrays.asList(), null)//
						)//
				));
		nodes.add(//
				new MenuTreeNode("In Communication", "communication", "UTILITY", "/images/icon/communications_icon.svg", //
						new MenuTreeNode("IN COMMUNICATION", "communication", //
								new MenuTreeNode("CM3010", "Support Center", "support_center", "20221008", //
										"/support/customer-support", "/images/icon/customer-support.svg", //
										true, true, true, true, //
										false, Arrays.asList("OEM_REGULAR", "SUPPLIER_REGULAR", "SUPPLIER_INSTALLATION"), null), //
								new MenuTreeNode("CM4010", "Tutorials", "tutorials", "20221008", //
										"/support/tutorial", "/images/icon/tutorials.svg", //
										true, true, true, true, //
										false, Arrays.asList("OEM_REGULAR", "SUPPLIER_REGULAR", "SUPPLIER_INSTALLATION"), null), //
								new MenuTreeNode("CM2080", "Version History", "version_history", "20221008", //
										"/support/version-history", "/images/icon/version.svg", //
										true, false, false, false, //
										false, null, null) //
						)//
				));
		return nodes;
	}

}
