package saleson.common.event.alert;

import saleson.common.enumeration.AlertType;
import saleson.model.Mold;
import saleson.model.MoldEfficiency;

public class EfficiencyAlertEvent implements AlertEvent<MoldEfficiency> {
	private MoldEfficiency moldEfficiency;

	public EfficiencyAlertEvent(MoldEfficiency moldEfficiency) {
		this.moldEfficiency = moldEfficiency;
	}

	@Override
	public MoldEfficiency get() {
		return this.moldEfficiency;
	}

	@Override
	public AlertType getAlertType() {
		return AlertType.EFFICIENCY;
	}

	@Override
	public Mold getMold() {
		return moldEfficiency.getMold();
	}
}
