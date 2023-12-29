package saleson.api.batch.payload;

import lombok.Data;
import saleson.api.machineDowntimeAlert.payload.SearchMachineDowntimePayload;
import saleson.api.mold.payload.MoldPayload;
import saleson.api.terminal.payload.TerminalAlertPayload;
import saleson.common.enumeration.*;

@Data
public class BatchPayload {
	private PageType pageType;
	private String query;
	private String status;
	private Boolean lastAlert;
	private OperatingStatus operatingStatus;
	private EquipmentStatus equipmentStatus;
	private Boolean maintenanced;
	private MaintenanceStatus maintenanceStatus;
	private NotificationStatus notificationStatus;
	private SpecialAlertType specialAlertType;
	private String extraStatus;
	private Boolean locationChanged;
	private Boolean inList;
	private String tab;

	public MoldPayload toMoldPayload() {
		MoldPayload result = new MoldPayload();
		result.setQuery(getQuery());
		result.setStatus(getStatus());
		result.setLastAlert(getLastAlert());
		result.setOperatingStatus(getOperatingStatus());
		result.setEquipmentStatus(getEquipmentStatus());
		result.setMaintenanced(getMaintenanced());
		result.setMaintenanceStatus(getMaintenanceStatus());
		result.setNotificationStatus(getNotificationStatus());
		result.setSpecialAlertType(getSpecialAlertType());
		result.setExtraStatus(getExtraStatus());
		result.setLocationChanged(getLocationChanged());
		result.setInList(getInList());
		return result;
	}

	public TerminalAlertPayload toTerminalAlertPayload() {
		TerminalAlertPayload result = new TerminalAlertPayload();
		result.setQuery(getQuery());
		result.setStatus(getStatus());
		result.setLastAlert(getLastAlert());
		result.setOperatingStatus(getOperatingStatus());
		result.setEquipmentStatus(getEquipmentStatus());
		result.setNotificationStatus(getNotificationStatus());
		return result;
	}

	public SearchMachineDowntimePayload toMachineDowntimePayload() {
		SearchMachineDowntimePayload result = new SearchMachineDowntimePayload();
		result.setQuery(getQuery());
		result.setTab(getTab());
		result.setLastAlert(getLastAlert());
		return result;
	}
}
