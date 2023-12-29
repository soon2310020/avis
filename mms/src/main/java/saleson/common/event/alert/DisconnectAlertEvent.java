package saleson.common.event.alert;

import saleson.common.enumeration.AlertType;
import saleson.model.Mold;
import saleson.model.MoldDisconnect;

public class DisconnectAlertEvent implements AlertEvent<MoldDisconnect> {
	private MoldDisconnect moldDisconnect;

	public DisconnectAlertEvent(MoldDisconnect moldDisconnect) {
		this.moldDisconnect = moldDisconnect;
	}

	@Override
	public MoldDisconnect get() {
		return this.moldDisconnect;
	}

	@Override
	public AlertType getAlertType() {
		return AlertType.DISCONNECTED;
	}

	@Override
	public Mold getMold() {
		return moldDisconnect.getMold();
	}
}
