package saleson.common.event.alert;

import saleson.common.enumeration.AlertType;
import saleson.model.Mold;
import saleson.model.MoldMisconfigure;

public class MisconfigureAlertEvent implements AlertEvent<MoldMisconfigure> {
	private MoldMisconfigure moldMisconfigure;

	public MisconfigureAlertEvent(MoldMisconfigure moldMisconfigure) {
		this.moldMisconfigure = moldMisconfigure;
	}

	@Override
	public MoldMisconfigure get() {
		return this.moldMisconfigure;
	}

	@Override
	public AlertType getAlertType() {
		return AlertType.MISCONFIGURE;
	}

	@Override
	public Mold getMold() {
		return moldMisconfigure.getMold();
	}
}
