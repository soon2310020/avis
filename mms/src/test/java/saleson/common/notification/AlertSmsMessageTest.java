package saleson.common.notification;

import org.junit.Test;
import saleson.common.enumeration.AlertType;

import static org.assertj.core.api.Assertions.assertThat;

public class AlertSmsMessageTest {

	@Test
	public void builderTest() {
		Message message  = AlertSmsMessage.builder()
				.content("Good")
				.alertType(AlertType.MISCONFIGURE)
				.build();

		assertThat(message).isNotNull();
	}

}