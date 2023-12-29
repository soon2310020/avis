package saleson.service.transfer;

import java.util.List;

import saleson.api.chart.payload.DashboardFilterPayload;
import saleson.model.data.LogDisconnectionData;

public interface LogDisconnectionRepositoryCustom {
	List<LogDisconnectionData> findLogDisconnectionData(DashboardFilterPayload payload);
}
