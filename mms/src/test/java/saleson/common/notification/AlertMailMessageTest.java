package saleson.common.notification;

import org.junit.Test;
import saleson.common.enumeration.AlertType;

import static org.assertj.core.api.Assertions.assertThat;

public class AlertMailMessageTest {
	@Test
	public void builderTest() {
		Message message  = AlertMailMessage.builder()
				.alertType(AlertType.MISCONFIGURE)
				.title("asdfasdf")
				.build();

		assertThat(message).isNotNull();
	}
}