package saleson.common.notification;

import lombok.*;
import org.springframework.beans.factory.annotation.Autowired;
import saleson.common.config.Const;
import saleson.common.enumeration.MaintenanceStatus;
import saleson.common.enumeration.NotificationStatus;
import saleson.common.enumeration.PeriodType;
import saleson.common.util.DateUtils;
import saleson.common.util.StringUtils;
import saleson.model.*;
import saleson.service.mail.AlertEmailService;
import saleson.service.util.DateTimeUtils;

import java.util.ArrayList;
import java.util.Arrays;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper=false)
public class SystemMailMessage extends MailMessage {
    private String host;
    private PeriodType periodType;
    private List<MoldLocation> moldLocations;
    private List<MoldDisconnect> moldDisconnects;
    private List<TerminalDisconnect> terminalDisconnects;
    private List<MoldCycleTime> moldCycleTimes;
    private List<MoldMaintenance> moldMaintenances;
    private List<MoldCorrective> moldCorrectives;
    private List<MoldEfficiency> moldEfficiencies;
    private List<MoldMisconfigure> moldMisconfigures;
    private List<MoldDowntimeEvent> moldDowntimeEvents;
    private List<MoldDataSubmission> moldDataSubmissions;
    private List<User> users;
    private Boolean confirmRegistration;
    private Boolean reminderUpcoming;
    private Boolean reminderOverdue;
    AlertEmailService alertEmailService;

    @Builder
    private SystemMailMessage(List<String> receivers, String title, String content, PeriodType periodType, String host,
                              List<MoldLocation> moldLocations, List<MoldDisconnect> moldDisconnects, List<TerminalDisconnect> terminalDisconnects,
                              List<MoldCycleTime> moldCycleTimes, List<MoldMaintenance> moldMaintenances, List<MoldCorrective> moldCorrectives,
                              List<MoldEfficiency> moldEfficiencies, List<MoldMisconfigure> moldMisconfigures,
                              List<MoldDataSubmission> moldDataSubmissions, List<MoldDowntimeEvent> moldDowntimeEvents, List<User> users, Boolean confirmRegistration,
                              Boolean reminderUpcoming, Boolean reminderOverdue,AlertEmailService alertEmailService){
        super(receivers, title, content);
        this.host = host;
        this.periodType = periodType;
        this.moldLocations = moldLocations;
        this.moldDisconnects = moldDisconnects;
        this.terminalDisconnects = terminalDisconnects;
        this.moldCycleTimes = moldCycleTimes;
        this.moldMaintenances = moldMaintenances;
        this.moldCorrectives = moldCorrectives;
        this.moldEfficiencies = moldEfficiencies;
        this.moldMisconfigures = moldMisconfigures;
        this.moldDataSubmissions = moldDataSubmissions;
        this.moldDowntimeEvents = moldDowntimeEvents;
        this.users = users;
        this.confirmRegistration = confirmRegistration;
        this.reminderUpcoming = reminderUpcoming;
        this.reminderOverdue = reminderOverdue;
        this.alertEmailService=alertEmailService;
    }

    @Override
    public String getTitle() {
        String yesterday = DateUtils.getYesterday("MMM dd, yyyy");
        String yest = DateUtils.getYesterday("yyyy-MM-dd HH:mm");
        if(periodType.equals(PeriodType.DAILY)){
            return "Alerts For " + yesterday;
        }else if(periodType.equals(PeriodType.MONTHLY)){
            return "Alerts For " + DateTimeUtils.getFullMonthName(yesterday.substring(0, 3)) + " " + yesterday.substring(8);
        }else if(periodType.equals(PeriodType.WEEKLY)){
            return "Alerts For Week " + DateUtils.getWeekYear(yest, "yyyy-MM-dd HH:mm");
        }else {
            if(moldDisconnects != null && !moldDisconnects.isEmpty()) return "Alerts for Tooling Disconnection";
            else if(terminalDisconnects != null && !terminalDisconnects.isEmpty()) return "Alerts for Terminal Disconnection";
            else if(moldMisconfigures != null && !moldMisconfigures.isEmpty()) return "Alerts for Reset Request";
            else if(moldCorrectives != null && !moldCorrectives.isEmpty()) {
                if(reminderUpcoming != null && reminderUpcoming == true) {
                    if(moldCorrectives.size() == 1)
                        return "You have one CM waiting to be completed";
                    return "You have " + moldCorrectives.size() + " CMs waiting to be completed";
                }
                if(reminderOverdue != null && reminderOverdue == true) {
                    if(moldCorrectives.size() == 1)
                        return "One CM has passed its due date";
                    return moldCorrectives.size() + " CMs have passed its due date";
                }
                if(moldCorrectives.size() == 1)
                    return "One tooling was reported failure";
                return moldCorrectives.size() + " toolings were reported failure";
            }
            else if(moldDataSubmissions != null && !moldDataSubmissions.isEmpty()) return "Alert for Data Submission";
            else if(users != null && !users.isEmpty()) return "You have a new user requesting access to the system";
            else if(confirmRegistration != null){
                if(confirmRegistration)
                    return "Your access request has been approved";
                else
                    return "Your access request has been declined";
            }
            return "Alerts Real Time";
        }
    }




    private String getNameNew() {
        String yesterday = DateUtils.getYesterday("MMM dd, yyyy");
        String yest = DateUtils.getYesterday("yyyy-MM-dd HH:mm");

        String title = "";
        String periodTypeName = "";
        if (periodType.equals(PeriodType.DAILY)) {
            title = "Summary of Alerts for " + yesterday;
            periodTypeName = "DAILY";

        } else if (periodType.equals(PeriodType.MONTHLY)) {
            title = "Summary of Alerts for Month " + DateTimeUtils.getFullMonthName(yesterday.substring(0, 3));
            periodTypeName = "MONTHLY";

        } else {

            if (moldDisconnects != null && !moldDisconnects.isEmpty()) {
                if (moldDisconnects.size() == 1) title = "1 tooling is currently disconnected";
                else title = moldDisconnects.size() + " toolings are currently disconnected";
            } else if (terminalDisconnects != null && !terminalDisconnects.isEmpty()) {
                if (terminalDisconnects.size() == 1) title = "1 terminal is currently disconnected";
                else title = terminalDisconnects.size() + " terminals are currently disconnected";
            } else if (moldMisconfigures != null && !moldMisconfigures.isEmpty()) {
                if (moldMisconfigures.size() == 1) title = "1 tooling is requesting reset";
                else title = moldMisconfigures.size() + " toolings are requesting reset";
            } else if (moldCorrectives != null && !moldCorrectives.isEmpty()) {
                if (reminderUpcoming != null && reminderUpcoming == true) {
                    if (moldCorrectives.size() == 1)
                        title = "You have one CM* waiting to be completed";
                    else title = "You have " + moldCorrectives.size() + " CMs* waiting to be completed";
                } else if (reminderOverdue != null && reminderOverdue == true) {
                    if (moldCorrectives.size() == 1)
                        title = "One CM* has passed its due date";
                    else
                        title = moldCorrectives.size() + " CMs* have passed its due date";
                } else {
                    title = "Tooling " + moldCorrectives.get(0).getMold().getEquipmentCode() + " was reported failure";
                }
            } else if (moldDataSubmissions != null && !moldDataSubmissions.isEmpty()) {
                if (moldDataSubmissions.size() == 1) {
                    if (moldDataSubmissions.get(0).getNotificationStatus().equals(NotificationStatus.PENDING)) {
                        title = "A tooling is waiting for approval";
                    } else if (moldDataSubmissions.get(0).getNotificationStatus().equals(NotificationStatus.APPROVED)) {
                        title = "Tooling " + moldDataSubmissions.get(0).getMold().getEquipmentCode() + " has been approved";
                    } else if (moldDataSubmissions.get(0).getNotificationStatus().equals(NotificationStatus.DISAPPROVED)) {
                        title = "Tooling " + moldDataSubmissions.get(0).getMold().getEquipmentCode() + " has been disapproved";
                    }
                } else title = moldDataSubmissions.size() + " toolings are requesting data submission";
            } else if (users != null && !users.isEmpty()) {
                if (users.size() == 1) title = "You have a new user requesting access to the system";
                else title = "You have " + users.size() + " new users requesting access to the system";
            } else if (confirmRegistration != null) {
                if (confirmRegistration)
                    title = "Your access request has been approved";
                else
                    title = "Your access request has been declined";
            }

        }
        String content = "    <div class=\"padding-title\"><h1>\n" +
                title +
                "    </h1></div>";
        if (!StringUtils.isEmpty(periodType)) {
            content += "    <div class=\"period-type \">\n" +
                    periodTypeName +
                    "    </div>";
        }
        return content;

    }
    @Override
    public String getContent(){
        String nameContent=getNameNew();
        String contentTables=getContentTables();

        String content = alertEmailService.generateMailContent(new Object[]{
                nameContent,
                contentTables
        });
        return content;
    }
    private String getContentTables(){
        String content ="";
        if(moldLocations != null && moldLocations.size() > 0) content += getMoldLocationNew();
        if(terminalDisconnects != null && terminalDisconnects.size() > 0) content += getTerminalDisconnectNew();
        if(moldDisconnects != null && moldDisconnects.size() > 0) content += getMoldDisconnectNew();
        if(moldCycleTimes != null && moldCycleTimes.size() > 0) content += getMoldCycleTimeNew();
        if(moldMaintenances != null && moldMaintenances.size() > 0) content += getMoldMaintenanceNew();
        if(moldCorrectives != null && moldCorrectives.size() > 0) content += getMoldCorrectiveNew();
        if(moldEfficiencies != null && moldEfficiencies.size() > 0) content += getMoldEfficiencyNew();
        if(moldMisconfigures != null && moldMisconfigures.size() > 0) content += getMoldMisconfigureNew();
        if(moldDataSubmissions != null && moldDataSubmissions.size() > 0) content += getMoldDataSubmissionNew();
        if(moldDowntimeEvents != null && moldDowntimeEvents.size() > 0) content += getMoldDowntimeNew();
        if(users != null && users.size() > 0) content += getUserNew();
        if(confirmRegistration != null) content += getLoginButtonNew();

        return content;
    }

    private String getMoldLocationNew() {
        String content[] = {getTableHeaderNew("RELOCATION", "TOOLING ID", "COMPANY", "CURRENT LOCATION", "RELOCATION DATE", "DETAILS")};
        moldLocations.forEach(moldLocation -> {
            content[0] += getTableBodyNew(moldLocation.getMold().getEquipmentCode(), moldLocation.getMold().getCompanyName(), moldLocation.getLocation().getName(),
                    moldLocation.getCreatedDateTime(), Const.ALERT_CENTER_PATH+"?menuKey="+Const.ALERT_PATH.mold_locations);
        });
        content[0] += contentTableEnd();
        content[0] += getTotalItemsNew("*In total, <strong>" + moldLocations.size() + "</strong> toolings were relocated.");
        return content[0];
    }

    private String getTerminalDisconnectNew(){
        String content[] = {getTableHeaderNew("DISCONNECTED TERMINAL", "TERMINAL ID", "COMPANY", "LOCATION", "LAST CONNECTION", "DETAILS")};
        if(periodType.equals(PeriodType.REAL_TIME)) content[0] = getTableHeaderNew("", "TERMINAL ID", "COMPANY", "LOCATION", "LAST CONNECTION", "DETAILS");
        terminalDisconnects.forEach(terminalDisconnect -> {
            String lastConnectTime=terminalDisconnect.getNotificationDateTime();
            content[0] += getTableBodyNew(terminalDisconnect.getTerminal().getEquipmentCode(), terminalDisconnect.getTerminal().getCompanyName(),
                    terminalDisconnect.getTerminal().getLocationName(), lastConnectTime, Const.ALERT_CENTER_PATH+"?menuKey="+Const.ALERT_PATH.mold_disconnected);
        });
        content[0] += contentTableEnd();
        if(!periodType.equals(PeriodType.REAL_TIME)) content[0] += getTotalItemsNew("*In total, <strong>" + terminalDisconnects.size() + "</strong> terminals were disconnected.");

        return content[0];
    }
    private String getMoldDisconnectNew(){
        String content[] = {getTableHeaderNew("DISCONNECTED TOOLING", "TOOLING ID", "COMPANY", "LOCATION", "LAST CONNECTION", "DETAILS")};
        if(periodType.equals(PeriodType.REAL_TIME)) content[0] = getTableHeaderNew("", "TOOLING ID", "COMPANY", "LOCATION", "LAST CONNECTION", "DETAILS");
        moldDisconnects.forEach(moldDisconnect -> {
            content[0] += getTableBodyNew(moldDisconnect.getMold().getEquipmentCode(), moldDisconnect.getMold().getCompanyName(),
                    moldDisconnect.getMold().getLocationName(), moldDisconnect.getNotificationDateTime(), Const.ALERT_CENTER_PATH+"?menuKey="+Const.ALERT_PATH.mold_disconnected);
        });
        content[0] += contentTableEnd();
        if(!periodType.equals(PeriodType.REAL_TIME)) content[0] += getTotalItemsNew("*In total, <strong>" + moldDisconnects.size() + "</strong> toolings were disconnected.");
        return content[0];
    }
    private String getMoldCycleTimeNew(){
        String content[] = {getTableHeaderNew("CYCLE TIME", "TOOLING ID", "COMPANY", "LOCATION", "CYCLE TIME STATUS", "DETAILS")};
        moldCycleTimes.forEach(moldCycleTime -> {
            content[0] += getTableBodyNew(moldCycleTime.getMold().getEquipmentCode(), moldCycleTime.getMold().getCompanyName(),
                    moldCycleTime.getMold().getLocationName(), moldCycleTime.getCycleTimeStatus().getTitle(), Const.ALERT_CENTER_PATH+"?menuKey="+Const.ALERT_PATH.mold_cycle_time);
        });
        content[0] += contentTableEnd();
        content[0] += getTotalItemsNew("*In total, <strong>" + moldCycleTimes.size() + "</strong> toolings were beyond the cycle time limits.");
        return content[0];
    }

    private String getMoldMaintenanceNew(){
        String content[] = {getTableHeaderNew("PREVENTIVE MAINTENANCE", "TOOLING ID", "COMPANY", "LOCATION", "MAINTENANCE STATUS", "DETAILS")};
        moldMaintenances.forEach(moldMaintenance -> {
            content[0] += getTableBodyNew(moldMaintenance.getMold().getEquipmentCode(), moldMaintenance.getMold().getCompanyName(),
                    moldMaintenance.getMold().getLocationName(), moldMaintenance.getMaintenanceStatus().getTitle(), Const.ALERT_CENTER_PATH+"?menuKey="+Const.ALERT_PATH.mold_maintenance);
        });
        content[0] += contentTableEnd();

        long upcoming = moldMaintenances.stream().filter(x -> x.getMaintenanceStatus().equals(MaintenanceStatus.UPCOMING)).count();
        long overdue = moldMaintenances.stream().filter(x -> x.getMaintenanceStatus().equals(MaintenanceStatus.OVERDUE)).count();
        content[0] += getTotalItemsNew("*In total, there are (is): <strong>" + upcoming + "</strong> upcoming maintenance ");
        content[0] += getTotalItemsNew("<strong>" + overdue + "</strong> overdue maintenance  ");

        return content[0];
    }

    private String getMoldCorrectiveNew(){
        String content[] = {getTableHeaderNew("CORRECTIVE MAINTENANCE", "TOOLING ID", "COMPANY", "LOCATION", "FAILURE TIME", "DETAILS")};
        if(periodType.equals(PeriodType.REAL_TIME)) content[0] = getTableHeaderNew("", "TOOLING ID", "COMPANY", "LOCATION", "FAILURE TIME", "DETAILS");
        moldCorrectives.forEach(moldCorrective -> {
            content[0] += getTableBodyNew(moldCorrective.getMold().getEquipmentCode(), moldCorrective.getMold().getCompanyName(),
                    moldCorrective.getMold().getLocationName(), moldCorrective.getFailureDateTime(), Const.ALERT_CENTER_PATH+"?menuKey="+Const.ALERT_PATH.mold_maintenance+"#cm");
        });
        content[0] += contentTableEnd();

        if(reminderUpcoming != null || reminderOverdue != null) content[0] += getTotalItemsNew("* Corrective Maintenance");
        return content[0];
    }

    private String getMoldEfficiencyNew(){
        String content[] = {getTableHeaderNew("UPTIME", "TOOLING ID", "COMPANY", "LOCATION", "UPTIME STATUS", "DETAILS")};
        moldEfficiencies.forEach(moldEfficiency -> {
            content[0] += getTableBodyNew(moldEfficiency.getMold().getEquipmentCode(), moldEfficiency.getMold().getCompanyName(),
                    moldEfficiency.getMold().getLocationName(), moldEfficiency.getEfficiencyStatus().getTitle(), Const.ALERT_CENTER_PATH+"?menuKey="+Const.ALERT_PATH.mold_efficiency);
        });
        content[0] += contentTableEnd();
        content[0] += getTotalItemsNew("*In total, <strong>" + moldEfficiencies.size() + "</strong> toolings were beyond efficiency limits.");
        return content[0];
    }

    private String getMoldMisconfigureNew(){
        String content[] = {getTableHeaderNew("RESET", "TOOLING ID", "COMPANY", "LOCATION", "RESET VALUE", "DETAILS")};
        if(periodType.equals(PeriodType.REAL_TIME)) content[0] = getTableHeaderNew("", "TOOLING ID", "COMPANY", "LOCATION", "RESET VALUE", "DETAILS");
        moldMisconfigures.forEach(moldMisconfigure -> {
            content[0] += getTableBodyNew(moldMisconfigure.getMold().getEquipmentCode(), moldMisconfigure.getMold().getCompanyName(),
                    moldMisconfigure.getMold().getLocationName(), moldMisconfigure.getPreset(), Const.ALERT_CENTER_PATH+"?menuKey="+Const.ALERT_PATH.mold_misconfigured);
        });
        content[0] += contentTableEnd();
        if(!periodType.equals(PeriodType.REAL_TIME)) content[0] += getTotalItemsNew("*In total, <strong>" + moldMisconfigures.size() + "</strong> toolings requested reset.");
        return content[0];
    }

    private String getMoldDowntimeNew(){
        String content[] = {getTableHeaderNew("DOWNTIME", "TOOLING ID", "COMPANY", "LOCATION", "DOWNTIME DURATION", "DETAILS")};
        moldDowntimeEvents.forEach(moldDowntimeEvent -> {
            String downtimeDuration = "";
            Long downtimeSeconds = moldDowntimeEvent.getDowntimeSeconds();
            if(downtimeSeconds < 24 * 3600){
                Long hours = downtimeSeconds / 3600;
                Long minutes = (downtimeSeconds - (downtimeSeconds / 3600) * 3600) / 60;
                String hourText = hours < 2 ? " hour " : " hours ";
                String minuteText = minutes < 2 ? " minute" : " minutes";
                downtimeDuration = hours + hourText + minutes + minuteText;
            }else if(downtimeSeconds < 24 * 7 * 3600){
                Long days = downtimeSeconds / (3600 * 24);
                String dayText = days < 2 ? " day" : " days";
                downtimeDuration = days + dayText;
            }else if(downtimeSeconds < 24 * 365 * 3600){
                Long weeks = downtimeSeconds / (3600 * 24 * 7);
                String weekText = weeks < 2 ? " week" : " weeks";
                downtimeDuration = weeks + weekText;
            }else{
                Long years = downtimeSeconds / (3600 * 24 * 365);
                String yearText = years < 2 ? " year": " years";
                downtimeDuration = years + yearText;
            }
            content[0] += getTableBodyNew(moldDowntimeEvent.getMold().getEquipmentCode(), moldDowntimeEvent.getMold().getCompanyName(),
                    moldDowntimeEvent.getMold().getLocationName(), downtimeDuration, Const.ALERT_CENTER_PATH+"?menuKey="+Const.ALERT_PATH.mold_downtime);
        });
        content[0] += contentTableEnd();
//        content[0] += getTotalItemsNew("*In total, <strong>" + moldDowntimeEvents.size() + "</strong> toolings were beyond the cycle time limits.");
        return content[0];
    }

    private String getMoldDataSubmissionNew(){
        String content[] = {getTableHeaderNew("DATA SUBMISSION", "TOOLING ID", "COMPANY", "LOCATION", "DETAILS")};
        if(periodType.equals(PeriodType.REAL_TIME)) content[0] = getTableHeaderNew("", "TOOLING ID", "COMPANY", "LOCATION", "DETAILS");
        moldDataSubmissions.forEach(moldDataSubmission -> {
            if(moldDataSubmission.getNotificationStatus().equals(NotificationStatus.APPROVED))
                content[0] += getTableBodyNew(moldDataSubmission.getMold().getEquipmentCode(), moldDataSubmission.getMold().getCompanyName(),
                        moldDataSubmission.getMold().getLocationName(), Const.ALERT_CENTER_PATH+"?menuKey="+Const.ALERT_PATH.mold_data_submission+"#APPROVED");
            else if(moldDataSubmission.getNotificationStatus().equals(NotificationStatus.DISAPPROVED))
                content[0] += getTableBodyNew(moldDataSubmission.getMold().getEquipmentCode(), moldDataSubmission.getMold().getCompanyName(),
                        moldDataSubmission.getMold().getLocationName(), Const.ALERT_CENTER_PATH+"?menuKey="+Const.ALERT_PATH.mold_data_submission+"#DISAPPROVED");
            else
                content[0] += getTableBodyNew(moldDataSubmission.getMold().getEquipmentCode(), moldDataSubmission.getMold().getCompanyName(),
                        moldDataSubmission.getMold().getLocationName(), Const.ALERT_CENTER_PATH+"?menuKey="+Const.ALERT_PATH.mold_data_submission);
        });
        content[0] += contentTableEnd();

        if(!periodType.equals(PeriodType.REAL_TIME)) content[0] += getTotalItemsNew("*In total, <strong>" + moldDataSubmissions.size() + "</strong> toolings requested data submission.");
        return content[0];
    }

	private String getUserNew() {
		String content[] = { getTableHeaderNew("REGISTRATION", "NAME", "EMAIL", "COMPANY", "POSITION", "DETAILS") };
		if (periodType.equals(PeriodType.REAL_TIME)) {
			content[0] = getTableHeaderNew("", "NAME", "EMAIL", "COMPANY", "POSITION", "DETAILS");
		}
		users.forEach(user -> {
			String memo = user.getMemo();
			if (memo != null && memo.startsWith("Company Name:")) {
				memo = memo.substring(memo.indexOf(":") + 2);
			}
			content[0] += getTableBodyNew(user.getName(), user.getEmail(), memo, user.getPosition(), "/admin/users/" + user.getId());
		});
		content[0] += contentTableEnd();
		if (!periodType.equals(PeriodType.REAL_TIME)) {
			content[0] += getTotalItemsNew("*In total, <strong>" + users.size() + "</strong> new users requesting access to the system.");
		}
		return content[0];
	}

    private String getLoginButtonNew() {
        String content =
                "    <div class=\"row-btn-center\">\n" +
                        "        <div class=\"btn-login\" >";
        if (this.confirmRegistration) {
            content += "                                                                <a href=\"" + host + "\" >Log in now</a>\n";
        } else {
            content += "                                                                <a href=\"" + host + "/register\" >Request again</a>\n";
        }
        content += " </div>\n" +
                "    </div>";
        return content;
    }

    private String tabelNameDiv(String tableName) {
        return "<div class=\"padding-title \">\n" +
                "        <h2>" + tableName + "</h2>\n" +
                "    </div>";
    }

    private String cellTableTitle(String colName) {
        return cellTableTitle(colName,116);
    }
    private String cellTableTitle(String colName,int width) {
        return "<td width=\""+width+"\">\n" +
                "                    <h3>\n" +
                colName +
                "                    </h3>\n" +
                "                </td>";
    }

    private String cellTablePathView(String path) {
        return "<td>\n" +
                "                    <div class=\"cel-btn\">\n" +
                "                        <div class=\"btn\">\n" +
                "                            <a href=\"" + host + path + "\" target=\"_blank\" >\n" +
                "                                View\n" +
                "                            </a>\n" +
                "                        </div>\n" +
                "                    </div>\n" +
                "                </td>";
    }
    private String contentTableStart(){
        return "<div class=\"block-table\">\n" +
                "        <table>\n" ;
    }
    private String contentTableEnd(){
        return "        </table>\n" +
                "    </div>";
    }
    private String getTableHeaderNew(String tableName, String first, String second, String third, String forth, String fifth) {
        String content = tabelNameDiv(tableName);
        content += contentTableStart() +
                "            <tr>";
        content += cellTableTitle(first);
        content += cellTableTitle(second);
        content += cellTableTitle(third);
        content += cellTableTitle(forth);
        content += cellTableTitle(fifth);
        content += "</tr>";
        return content;
    }
    private String getTableBodyNew(String first, String second, String third, String forth, String path){
        String content="<tr>";
        content+=cellTableTitle(first);
        content+=cellTableTitle(second);
        content+=cellTableTitle(third);
        content+=cellTableTitle(forth);
        content+=cellTablePathView(path);
        content+="</tr>";

        return content;
    }
    private String getTableHeaderNew(String tableName, String first, String second, String third, String forth) {
        String content = tabelNameDiv(tableName);
        content += contentTableStart() +
                "            <tr>";
        content += cellTableTitle(first,145);
        content += cellTableTitle(second,145);
        content += cellTableTitle(third,145);
        content += cellTableTitle(forth,145);
        content += "</tr>";
        return content;
    }
    private String getTableBodyNew(String first, String second, String third, String path){
        String content="<tr>";
        content+=cellTableTitle(first,145);
        content+=cellTableTitle(second,145);
        content+=cellTableTitle(third,145);
        content+=cellTablePathView(path);
        content+="</tr>";

        return content;
    }

    private String getTotalItemsNew(String text) {
        return "    <div class=\"total-item\">\n" +
                "        <p>\n" +
                text +
                "        </p>\n" +
                "    </div>";
    }

}
