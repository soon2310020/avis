package saleson.common.event.alert;

import saleson.common.enumeration.AlertType;
import saleson.model.Mold;
import saleson.model.MoldCycleTime;

public class CycleTimeAlertEvent implements AlertEvent<MoldCycleTime> {
	private MoldCycleTime moldCycleTime;

	public CycleTimeAlertEvent(MoldCycleTime moldCycleTime) {
		this.moldCycleTime = moldCycleTime;
	}

	@Override
	public MoldCycleTime get() {
		return this.moldCycleTime;
	}

	@Override
	public AlertType getAlertType() {
		return AlertType.CYCLE_TIME;
	}


	@Override
	public Mold getMold() {
		return moldCycleTime.getMold();
	}
}
