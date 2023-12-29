package saleson.common.constant;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SpecialSortProperty {
    public static List<String> partSortProperties = Arrays.asList("totalMolds", "activeMolds", "idleMolds",
            "inactiveMolds", "disconnectedMolds", "quantityProduced", "totalProduced","weight");
    public static List<String> moldCycleTimeSortProperties = Arrays.asList("variance");
    public static List<String> moldEfficiencySortProperties = Arrays.asList("variance");
    public static List<String> moldMaintenanceSortProperties = Arrays.asList("period", "executionRate", "lastShotCheckpoint", "dueDate","utilNextPm","shotUntilNextPM");
    public static List<String> moldSortProperties = Arrays.asList("part");
    public static List<String> terminalSortProperties = Arrays.asList("numberCounter");
    public static List<String> cdataCounterProperties = Arrays.asList("counter.equipmentCode", "counter.mold.equipmentCode", "message", "cdata.createdAt");
    public static List<String> locationSortProperties = Arrays.asList("numberTerminal");
    public static String moldEngineerInChargeSort="engineerInCharge";
    public static String customFieldSort ="customField";
    public static String alertCustomFieldSort ="mold.customField";
    public static String moldInactiveSort ="inactivePeriod";
    public static String moldAccumulatedShotSort ="accumulatedShot";
    public static String priority ="priority";
    public static List<String> moldSortPartProperties = Arrays.asList("part", "productAndCategory");
    public static String endOfLifeCycleAccumulatedShot ="mold.accumulatedShot";
    public static String preset ="preset";
    public static String operatingStatus ="operatingStatus";
    public static String lastAlertAt ="lastAlertAt";
    public static String slDepreciation ="slDepreciation";
    public static String upDepreciation ="upDepreciation";
    public static String upperTierCompanies ="upperTierCompanies";
    public static String machineCode ="machineCode";

    public static String category = "category";

    public static List<String> moldCounterStatusProperties = Arrays.asList("toolingStatus", "sensorStatus", "counterStatus");

    public static List<String> checklistSpecialFields = Arrays.asList("individualUser","assignedCompany","creator");
    public static List<String> moldCounterSubscription = Arrays.asList("activePeriod", "subscriptionStatus","subscriptionExpiry");

    public static String terminalConnectionStatus = "connectionStatus";


}
