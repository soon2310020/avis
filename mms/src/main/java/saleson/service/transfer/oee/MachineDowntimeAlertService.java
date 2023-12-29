package saleson.service.transfer.oee;

import saleson.model.Cdata;
import saleson.model.Statistics;

public interface MachineDowntimeAlertService {
	void proc(Statistics statistics);
}
