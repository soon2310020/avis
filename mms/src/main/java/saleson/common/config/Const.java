package saleson.common.config;

import com.emoldino.api.asset.resource.base.mold.enumeration.ToolingStatus;
import com.google.common.collect.Lists;
import saleson.common.enumeration.*;

import java.util.*;
import java.util.stream.Collectors;

public interface Const {
    public static String ADMIN_PATH = "/admin";
    public static String PROFILE_PATH = "/profile";
    public static String SUPPORT_PATH = "/support";
    public static String INSIGHT_PATH = "/insight";
    public static String REPORT_PATH = "/report";
    public static String MMS_PATH = "/mms";
    public static String MOLD_PATH = "/admin/molds";
    public static String PART_PATH = "/admin/parts";
    public static String MACHINE_PATH = "/admin/machines";
    public static String COMPANY_PATH = "/admin/companies";
    public static String CATEGORY_PATH = "/admin/categories";
    public static String LOCATION_PATH = "/admin/locations";
    public static String COUNTER_PATH = "/admin/counters";
    public static String TERMINAL_PATH = "/admin/terminals";
    public static String ALERT_CENTER_PATH = "/front/alert-center";
    public static String OVERVIEW_PATH = "/front/tabbed-overview";
    public static String OEE_CENTER_PATH = "/front/oee-center";


    String DOWNLOAD_APP = "/download-app";

	public static final String SUCCESS = "SUCCESS";
	public static final String FAIL = "FAIL";
	String HEADER_TOKEN = "token";
    static int WIDTH_WARNING=30000;
    int PRODUCT_LEVEL=3;
    int BRAND_LEVEL=2;
    int CATEGORY_LEVEL=1;

	interface SECUTIRY {
		long LOCK_TIME_DURATION = 24 * 60 * 60 * 1000; // 24 hours
	}
	interface SESSION{
	    String language="language";
    }

    interface NOTIFICATION_MESSAGE{
	    String USER_HAS_REQUESTED_PLATFORM = "USER_HAS_REQUESTED_PLATFORM";
    }

    interface ALERT_PATH {
        String mold_locations = "/front/mold-locations";
        String mold_downtime = "/front/mold-downtime";
        String mold_disconnected = "/front/mold-disconnected";
        String mold_cycle_time = "/front/mold-cycle-time";
        String mold_maintenance = "/front/mold-maintenance";
        String mold_efficiency = "/front/mold-efficiency";
        String mold_misconfigured = "/front/mold-misconfigured";
        String mold_data_submission = "/front/mold-data-submission";
        String mold_refurbishment = "/front/mold-refurbishment";
        String mold_detachment = "/front/mold-detachment";
    }
    Set<String> ALERT_KEYS=new HashSet<>(Arrays.asList(
            ALERT_PATH.mold_locations,
            ALERT_PATH.mold_downtime,
            ALERT_PATH.mold_disconnected,
            ALERT_PATH.mold_cycle_time,
            ALERT_PATH.mold_maintenance,
            ALERT_PATH.mold_efficiency,
            ALERT_PATH.mold_misconfigured,
            ALERT_PATH.mold_data_submission,
            ALERT_PATH.mold_refurbishment,
            ALERT_PATH.mold_detachment
    ));
	interface ERROR_CODE{
		String USER_NOT_EXIST="USER_NOT_EXIST";
		String ACCOUNT_NOT_ACTIVED="ACCOUNT_NOT_ACTIVED";
		String INVALID_PASSWORD="INVALID_PASSWORD";
		String USER_NOT_ACTIVE="USER_NOT_ACTIVE";
		String EMPTY_TOKEN="EMPTY_TOKEN";
		String USER_OF_SESSION_NOT_FOUND="USER_OF_SESSION_NOT_FOUND";
		String INVALID_TOKEN="INVALID_TOKEN";
		String NO_DATA = "NO_DATA";
		String DUPLICATED_EMAIL = "DUPLICATED_EMAIL";
		String EMAIL_EXISTS = "EMAIL_EXISTS";
        String INVALID_INVITATION = "INVALID_INVITATION";
    }

    Map<String, PageType> menuSystemNoteMap = new HashMap<String, PageType>() {{
        put("/admin/roles", PageType.ACCESS_GROUP);
        put(CATEGORY_PATH, PageType.CATEGORY_SETTING);
        put(COMPANY_PATH, PageType.COMPANY_SETTING);
        put(COUNTER_PATH, PageType.COUNTER_SETTING);
        put(ALERT_PATH.mold_cycle_time, PageType.CYCLE_TIME_ALERT);
        put(ALERT_PATH.mold_data_submission, PageType.DATA_SUBMISSION_ALERT);
        put(ALERT_PATH.mold_disconnected, PageType.DISCONNECTION_ALERT);
        put(LOCATION_PATH, PageType.LOCATION);
        put(ALERT_PATH.mold_maintenance, PageType.MAINTENANCE_ALERT);
        put("/parts", PageType.PART_DASHBOARD);
        put(PART_PATH, PageType.PART_SETTING);
        put(ALERT_PATH.mold_refurbishment, PageType.REFURBISHMENT_ALERT);
        put(ALERT_PATH.mold_locations, PageType.RELOCATION_ALERT);
        put("/admin/presets", PageType.RESET);
        put(ALERT_PATH.mold_misconfigured, PageType.RESET_ALERT);
        put("/support/customer-support", PageType.SUPPORT_CENTER);
        put(TERMINAL_PATH, PageType.TERMINAL_SETTING);
        put("/molds", PageType.TOOLING_DASHBOARD);
        put(MOLD_PATH, PageType.TOOLING_SETTING);
        put(ALERT_PATH.mold_efficiency, PageType.EFFICIENCY_ALERT);
        put("/admin/users", PageType.USER);
        put(ALERT_PATH.mold_detachment, PageType.DETACHMENT_ALERT);
        put("/insight", PageType.END_OF_LIFE_CYCLE);
        put("/report/capacity-utilization", PageType.CAPACITY_UTILIZATION_REPORT);
        put("/report/cycle-time", PageType.CYCLE_TIME_REPORT);
        put("/report/production-efficiency", PageType.PRODUCTION_EFFICIENCY_REPORT);
//        put("/admin/checklist", PageType.CHECKLIST_MAINTENANCE);
        put("/admin/checklist-center?systemNoteFunction=CHECKLIST_MAINTENANCE", PageType.CHECKLIST_MAINTENANCE);
        put("/admin/checklist-center?systemNoteFunction=CHECKLIST_REJECT_RATE", PageType.CHECKLIST_REJECT_RATE);
        put("/admin/checklist-center?systemNoteFunction=CHECKLIST_REFURBISHMENT", PageType.CHECKLIST_REFURBISHMENT);
        put(ALERT_PATH.mold_downtime, PageType.DOWNTIME_ALERT);

    }};
    Map<String, ObjectType> menuObjectTypeMap = new HashMap<String, ObjectType>() {{
        put("/admin/roles", ObjectType.ACCESS_GROUP);
//		put("/admin/roles",ObjectType.ACCESS_FEATURE);
        put(CATEGORY_PATH, ObjectType.CATEGORY);
        put(COMPANY_PATH, ObjectType.COMPANY);
        put(COUNTER_PATH, ObjectType.COUNTER);
        put(LOCATION_PATH, ObjectType.LOCATION);
        put(PART_PATH, ObjectType.PART);
        put(TERMINAL_PATH, ObjectType.TERMINAL);
        put(MOLD_PATH, ObjectType.TOOLING);
        put("/admin/users", ObjectType.USER);
    }};

    interface OTE {
        Integer HIGH = 70;
        Integer MEDIUM = 30;
        Integer LOW = 0;
    }

    interface DEVICE_TYPE {
        String ANDROID = "ANDROID";
        String IOS = "IOS";
    }

    interface NOTIFICATION_TYPE {
        String ALERT = "ALERT";
        String NOTE = "NOTE";
    }

    interface LANGUAGE {
        String en = "en";
        String zh = "zh";
        String de = "de";
    }

    interface Column {
        interface tooling {
            interface TOOLING_BASIC {
                String Tooling_ID = "Tooling ID";
                String Counter_ID = "Sensor ID";
                String Accumulated_shot = "Accumulated Shots";
                String Tooling_Letter = "Tooling Letter";
                String Tooling_Type = "Tooling Type";
                String Tooling_Complexity = "Tooling Complexity";
                String Forecasted_Max_Shot = "Forecasted Max Shot";
                String Forecasted_Tool_Life_year = "Forecasted Tool Life (year)";
                String Year_of_Tool_Made = "Year of Tool Made";
                String Location_ID_Location = "Plant ID (Plant)";
                String Engineer_in_Charge_Email_Address = "Engineer in Charge (Email Address)";
                String Plant_Engineer_in_Charge_Email_Address = "Plant Engineer in Charge (Email Address)";
                String Tool_Description = "Tool Description";

            }

            interface TOOLING_PHYSICAL {
                String Tool_Size = "Tool Size (W x L x H)";//for export
                String Tool_Size_W = "Tool Size (W)";
                String Tool_Size_L = "Tool Size (L)";
                String Tool_Size_H = "Tool Size (H)";
                String Tool_Size_Unit = "Tool Size Unit";
                String Tool_Weight = "Tool Weight";
                String Tool_Weight_Unit = "Tool Weight Unit";
                String Shot_Weight = "Shot Weight";//for export
                String Toolmaker_ID_Toolmaker = "Toolmaker ID (Toolmaker)";
                String Injection_Molding_Machine_ID = "Injection Molding Machine ID";
                String Machine_Tonnage_ton = "Machine Tonnage (ton)";
                String Machine_Tonnage = "Machine Tonnage";//export
                String Total_Number_of_Cavities = "Total Number of Cavities";//export

            }

            interface TOOLING_RUNNER_SYSTEM {
                String Type_of_Runner_System = "Type of Runner System";
                String Maker_of_Runner_System = "Maker of Runner System";
                String Weight_of_Runner_System_gram = "Weight of Runner System (gram)";
                String Weight_of_Runner_System = "Weight of Runner System";//export
                String Hot_Runner_Number_of_Drop = "Hot Runner Number of Drop";
                String Hot_Runner_Zone = "Hot Runner Zone";

            }

            interface TOOLING_COST {
                String Cost_of_Tooling = "Cost of Tooling";
                String Cost_of_Tooling_Currency = "Cost of Tooling Currency";
                String salvageValue = "Salvage Value";
                String poDate = "PO Date";
                String poNumber = "PO Number";
                String Memo = "Memo";

            }

            interface TOOLING_SUPPLIER {
                String Supplier_ID_Supplier_Name = "Supplier ID (Supplier Name)";
                String Supplier = "Supplier";//export
                String Required_Labor = "Required Labor";
                String Production_Hour_per_Day_default_24_hours = "Production Hour per Day (default 24 hours)";
                String Production_Hour_per_Day = "Production Hour per Day";//export
                String Production_Day_per_Week_default_7_days = "Production Day per Week (default 7 days)";
                String Production_Day_per_Week = "Production Day per Week";//export

            }

            interface TOOLING_PRODUCTION {
                String Approved_Cycle_Time_second = "Approved Cycle Time (ACT)";
                String Toolmaker_Approved_Cycle_Time_second = "Toolmaker ACT";
                String Supplier_Approved_Cycle_Time_second = "Supplier ACT";
                String Cycle_Time_Tolerance_L1 = "Cycle Time Tolerance L1";
                String Cycle_Time_Tolerance_L1_Unit = "Cycle Time Tolerance L1 Unit";
                String Cycle_Time_Tolerance_L2 = "Cycle Time Tolerance L2";
                String Cycle_Time_Tolerance_L2_Unit = "Cycle Time Tolerance L2 Unit";
                String Uptime_Target_percent = "Uptime Target (%)";
                String Uptime_Tolerance_L1_percent = "Uptime Tolerance L1 (%)";
                String Uptime_Tolerance_L2_percent = "Uptime Tolerance L2 (%)";

            }

            interface TOOLING_MAINTENANCE {

                String Maintenance_Interval_default_50000 = "Maintenance Interval (default 50000)";
                String Upcoming_Maintenance_Tolerance_default_10000 = "Upcoming Maintenance Tolerance (default 10000)";
                String Overdue_Maintenance_Tolerance_default_5000 = "Overdue Maintenance Tolerance (default 5000)";

            }

            interface TOOLING_PART {
                String Part_ID = "Part ID";
                String Number_of_Cavity = "Number of Cavity";

            }

        }

        interface part {

        }

    }
    interface ColumnCode {
        interface tooling {
            interface BASIC {
                String equipmentCode = "equipmentCode";
                String toolingLetter = "toolingLetter";
                String toolingType = "toolingType";
                String toolingComplexity = "toolingComplexity";
                String designedShot = "designedShot";
                String lifeYears = "lifeYears";
                String madeYear = "madeYear";
                String locationId = "locationId";
                String engineers = "engineers";
                String plantEngineers = "plantEngineers";
                String toolDescription = "toolDescription";
            }

            interface PHYSICAL {
                String size = "size";

                String weight = "weight";

                String shotSize = "shotSize";
                String toolMakerCompanyName = "toolMakerCompanyName";
                String injectionMachineId = "injectionMachineId";
                String quotedMachineTonnage = "quotedMachineTonnage";
                String toolingPictureFile = "toolingPictureFile";
                String totalCavities = "totalCavities";
                interface IMPORT{
                    //for size
                    String size_w = "size_w";
                    String size_l = "size_l";
                    String size_h = "size_h";
                    String sizeUnit = "sizeUnit";
                    //for weight
                    String weightUnit = "weightUnit";
                }
            }

            interface RUNNER_SYSTEM {
                String runnerType = "runnerType";
                String runnerMaker = "runnerMaker";
                String weightRunner = "weightRunner";
                String hotRunnerDrop = "hotRunnerDrop";
                String hotRunnerZone = "hotRunnerZone";
            }

            interface COST {
                String cost = "cost";
                String salvageValue = "salvageValue";
                String poDate = "poDate";
                String poNumber = "poNumber";
                String memo = "memo";

                String costCurrencyType = "costCurrencyType";//for template
                String accumulatedMaintenanceCost = "accumulatedMaintenanceCost";//for export

            }

            interface SL_DEPRECIATION {
                String slCurrentBookValue = "slCurrentBookValue";
                String slDepreciation = "slDepreciation";
                String slDepreciationTerm = "slDepreciationTerm";
                String slYearlyDepreciation = "slYearlyDepreciation";
                String slLatestDepreciationPoint = "slLatestDepreciationPoint";
            }

            interface UP_DEPRECIATION {
                String upCurrentBookValue = "upCurrentBookValue";
                String depreciationPercentage = "depreciationPercentage";
                String upDepreciationTerm = "upDepreciationTerm";
                String upDepreciationPerShot = "upDepreciationPerShot";
                String upLatestDepreciationPoint = "upLatestDepreciationPoint";
            }


            interface SUPPLIER {
                String supplierCompanyName = "supplierCompanyName";
                String labour = "labour";
                String shiftsPerDay = "shiftsPerDay";
                String productionDays = "productionDays";
                String maxCapacityPerWeek = "maxCapacityPerWeek";

            }

            interface PRODUCTION {
                String approvedCycleTime = "approvedCycleTime";

                String toolmakerApprovedCycleTime = "toolmakerApprovedCycleTime";
                String cycleTimeLimit1 = "cycleTimeLimit1";
                String cycleTimeLimit2 = "cycleTimeLimit2";
                String uptimeTarget = "uptimeTarget";
                String uptimeLimitL1 = "uptimeLimitL1";
                String uptimeLimitL2 = "uptimeLimitL2";
                interface IMPORT{
                    String cycleTimeLimit1Unit = "cycleTimeLimit1Unit";
                    String cycleTimeLimit2Unit = "cycleTimeLimit2Unit";

                }

            }

            interface MAINTENANCE {

                String preventCycle = "preventCycle";
                String preventUpcoming = "preventUpcoming";
                String preventOverdue = "preventOverdue";
                String documentFiles = "documentFiles";
                String instructionVideo = "instructionVideo";
            }

            interface REFURBISHMENT {
                String lastMaintenanceDate = "lastMaintenanceDate";
            }

            interface WORK_ORDER {
                String lastRefurbishmentDate = "lastRefurbishmentDate";
            }

            interface PART {
                String partId = "partId";
                String cavity = "cavity";
                String totalCavities = "totalCavities";
            }

        }

        interface part {
            String category="category";
            String partCode="partCode";
            String name="name";
            String resinCode="resinCode";
            String resinGrade="resinGrade";
            String designRevision="designRevision";
            String size="size";
            String weight="weight";
            String weeklyDemand="weeklyDemand";
            String partPictureFile="partPictureFile";
            String quantityRequired="quantityRequired";

            interface EXPORT {
                String projectName="projectName";
                String categoryName="categoryName";
            }
            interface IMPORT {
                String size_w = "size_w";
                String size_d = "size_d";
                String size_h = "size_h";
                String sizeUnit = "sizeUnit";
                String weightUnit = "weightUnit";

                String productName = "productName";
            }
        }

    }
    interface DYNAMIC_DATA{
        interface TOOLING{
            String SHORT_COUNT="SHORT_COUNT";
            String CYCLE_TIME="CYCLE_TIME";
            String UPTIME="UPTIME";
            String TEMPERATURE="TEMPERATURE";
        }
    }
    interface DASHBOARD_GROUP_DEFAULT{
        List<GraphType> ASSET_MANAGEMENT_DASHBOARD= Arrays.asList(
                GraphType.ACTUAL_TARGET_UPTIME_RATIO
                ,GraphType.DISTRIBUTION
                ,GraphType.DOWNTIME
                //,GraphType.IMPLEMENTATION_STATUS
//                ,GraphType.OEE
                ,GraphType.OTE
                ,GraphType.HIERARCHY
                ,GraphType.QUICK_STATS
                ,GraphType.TOOLING
                ,GraphType.UTILIZATION_RATE
        );
        List<GraphType> SUPPLIER_PERFORMANCE_MANAGEMENT_DASHBOARD= Arrays.asList(
//                GraphType.CAPACITY,
                GraphType.CYCLE_TIME
                ,GraphType.CYCLE_TIME_STATUS
                ,GraphType.MAXIMUM_CAPACITY
                ,GraphType.ON_TIME_DELIVERY
                ,GraphType.MAINTENANCE_STATUS
                ,GraphType.PRODUCTION_PATTERN_ANALYSIS
                ,GraphType.PRODUCTION_RATE
                ,GraphType.TOOLING
                ,GraphType.UPTIME_STATUS
        );
    }

    interface numberLine{
        int LOCATION=5;
        int TOOLING=38;
        int COMPANY=8;
        int CATEGORY=2;
        int PROJECT=5;
        int PART=10;
        int MACHINE=8;
    }
    interface MENU_ID{
        Long ROOT=0L;
        Long ALERT=1200L;
    }

    interface DASHBOARD_SETTING_DEFAULT {
        Integer FIRST_LEVEL = 12;
        Integer SECOND_LEVEL = 24;
        Integer THIRD_LEVEL = 36;
    }

    interface TAB_TABLE_DEFAULT_TAB {
        List<String> TOOLING_DYSON = Arrays.asList("All", "PENDING", "APPROVED", "DISAPPROVED", TabType.DISPOSED.name(), "DELETED");
        List<String> TOOLING_OTHER = Arrays.asList("All", "DIGITAL", "NON_DIGITAL", TabType.DISPOSED.name(), "DELETED");

        List<String> PART = Arrays.asList("Enabled", "Disabled");

        List<String> LOCATION = Lists.newArrayList("Enabled", "Disabled");

        List<String> COMPANY = Lists.newArrayList("Enabled", "Disabled");

        List<String> Machine = Lists.newArrayList("All", "Matched", "Un-matched", "Disabled");
        List<String> User = Lists.newArrayList("Enabled","In-house","Supplier","Toolmaker","Disabled");
        List<String> Terminal = Lists.newArrayList("All","In-house","Supplier","Toolmaker","Disabled");
        List<String> Counter = Lists.newArrayList("All","In-house","Supplier","Toolmaker","Disabled");
    }

}
