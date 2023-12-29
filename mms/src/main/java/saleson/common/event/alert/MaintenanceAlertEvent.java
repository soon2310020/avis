package saleson.common.event.alert;

import saleson.common.enumeration.AlertType;
import saleson.model.Mold;
import saleson.model.MoldMaintenance;

public class MaintenanceAlertEvent implements AlertEvent<MoldMaintenance> {
	private MoldMaintenance moldMaintenance;

	public MaintenanceAlertEvent(MoldMaintenance moldMaintenance) {
		this.moldMaintenance = moldMaintenance;
	}

	@Override
	public MoldMaintenance get() {
		return this.moldMaintenance;
	}

	@Override
	public AlertType getAlertType() {
		return AlertType.MAINTENANCE;
	}

	@Override
	public Mold getMold() {
		return moldMaintenance.getMold();
	}
}
