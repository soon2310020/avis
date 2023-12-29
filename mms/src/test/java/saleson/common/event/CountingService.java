package saleson.common.event;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import saleson.common.event.alert.AlertEvent;
import saleson.common.event.alert.EfficiencyAlertEvent;
import saleson.common.event.alert.MisconfigureAlertEvent;
import saleson.model.MoldEfficiency;
import saleson.model.MoldMisconfigure;

@Service
public class CountingService {
	private ApplicationEventPublisher applicationEventPublisher;

	@Autowired
	public CountingService(ApplicationEventPublisher applicationEventPublisher) {
		this.applicationEventPublisher = applicationEventPublisher;
	}

	public void count(int times) {
		/*MoldMisconfigure moldMisconfigure = new MoldMisconfigure();
		moldMisconfigure.setId(111111L);

		AlertEvent alertEventx = new MisconfigureAlertEvent(moldMisconfigure);
		applicationEventPublisher.publishEvent(alertEventx);


		MoldEfficiency moldEfficiency = new MoldEfficiency();
		moldEfficiency.setId(2222L);

		AlertEvent alertEvent2 = new EfficiencyAlertEvent(moldEfficiency);
		applicationEventPublisher.publishEvent(alertEvent2);*/
	}
}
