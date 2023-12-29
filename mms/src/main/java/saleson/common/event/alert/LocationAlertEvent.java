package saleson.common.event.alert;

import saleson.common.enumeration.AlertType;
import saleson.model.Mold;
import saleson.model.MoldLocation;

public class LocationAlertEvent implements AlertEvent<MoldLocation> {
	private MoldLocation moldLocation;

	public LocationAlertEvent(MoldLocation moldLocation) {
		this.moldLocation = moldLocation;
	}

	@Override
	public MoldLocation get() {
		return this.moldLocation;
	}

	@Override
	public AlertType getAlertType() {
		return AlertType.RELOCATION;
	}

	@Override
	public Mold getMold() {
		return moldLocation.getMold();
	}
}
