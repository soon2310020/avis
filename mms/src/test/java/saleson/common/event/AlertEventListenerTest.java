package saleson.common.event;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.context.junit4.SpringRunner;
import saleson.common.event.alert.AlertEvent;
import saleson.common.event.alert.EfficiencyAlertEvent;
import saleson.common.event.alert.MisconfigureAlertEvent;
import saleson.model.MoldEfficiency;
import saleson.model.MoldMisconfigure;

import java.util.List;

import static org.hamcrest.core.IsInstanceOf.instanceOf;

@RunWith(SpringRunner.class)
@SpringBootTest
public class AlertEventListenerTest {
	@MockBean
	private ApplicationEventPublisher applicationEventPublisher;


	private CountingService countingService;

	@Captor
	protected ArgumentCaptor<Object> publishEventCaptor;

	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
		countingService = new CountingService(applicationEventPublisher);
	}

	@Test
	public void test() {
		MoldMisconfigure moldMisconfigure = new MoldMisconfigure();
		moldMisconfigure.setId(111111L);

		AlertEvent alertEvent1 = new MisconfigureAlertEvent(moldMisconfigure);
		applicationEventPublisher.publishEvent(alertEvent1);


		MoldEfficiency moldEfficiency = new MoldEfficiency();
		moldEfficiency.setId(2222L);

		AlertEvent alertEvent2 = new EfficiencyAlertEvent(moldEfficiency);
		applicationEventPublisher.publishEvent(alertEvent2);

		verifyPublishedEvents(alertEvent1, alertEvent2);
	}

	protected void verifyPublishedEvents(Object... events) {
		Mockito.verify(applicationEventPublisher, Mockito.times(events.length)).publishEvent(publishEventCaptor.capture());
		List<Object> capturedEvents = publishEventCaptor.getAllValues();

		for (int i = 0; i < capturedEvents.size(); i++) {
			Assert.assertThat(capturedEvents.get(i), instanceOf(events[i].getClass()));
			System.out.println(events[i].getClass().getName());
			Assert.assertEquals(capturedEvents.get(i), events[i]);
		}
	}
}