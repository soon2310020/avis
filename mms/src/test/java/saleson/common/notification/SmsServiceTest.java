package saleson.common.notification;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import saleson.common.enumeration.AlertType;

import java.util.Arrays;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest
public class SmsServiceTest {

	@Autowired
	SmsService smsService;

	@Test
	public void sendMail() {
		List<String> receivers = Arrays.asList("+82 01091099052", "+83 01091099125");
		Message message = AlertSmsMessage.builder()
				.receivers(receivers)
				.alertType(AlertType.MISCONFIGURE)
				.build();

		//smsService.send(message);
	}
}